package iBook.utils;

import javax.naming.*;
import javax.servlet.http.HttpSession;

import iBook.dao.CategoryDao;
import iBook.dao.UserDao;
import iBook.dao.UserPaymentDao;
import iBook.dao.factory.DaoFactory;
import iBook.dao.statefull.BookWishListBean;
import iBook.domain.Book;
import iBook.domain.User;
import iBook.dao.statefull.BookWishList;
import iBook.dao.BookDao;
import iBook.web.Page;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cache.infinispan.InfinispanRegionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.jboss.util.naming.NonSerializableFactory;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.Main;
import org.jnp.server.NamingServer;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for working with books and wishlist.
 */
public final class Utils {
    private static Utils instance = null;
    private static InitialContext ctx = null;
    private static Context context = null;
    private static final SessionFactory sessionFactory;
    private EmbeddedCacheManager cacheManager;

    private static final JBossStandaloneJTAManagerLookup lookup = new JBossStandaloneJTAManagerLookup();

    static {
        try {
           /* startJndiServer();
            context = createJndiContext();
            bindTransactionManager();
            bindUserTransaction();*/
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private Utils() {}

    public static Utils getInstance() {
        if(instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public EmbeddedCacheManager getCacheManager() throws IOException {
        if(cacheManager == null) {
            SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) getSessionFactory();
            InfinispanRegionFactory regionFactory = (InfinispanRegionFactory) sessionFactoryImpl.getSettings().getRegionFactory();
            cacheManager = regionFactory.getCacheManager();
        }

        return cacheManager;
    }

    /**
     * Returns the wishlist object.
     * @return      the wishlist statefull session bean.
     */
    public BookWishList getBookWishList(HttpSession session) {
        BookWishList wishList = null;
        try {
            User user = (User) session.getAttribute(Page.LOGGED_IN_USER);
            wishList = (BookWishList) getCacheManager().getCache("sfsb").get("wishList" + user.getId());
            if (wishList== null) {

                wishList = new BookWishListBean();
                wishList.init(user);
                getCacheManager().getCache("sfsb").put("wishList" + user.getId(), wishList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wishList;
    }

    /**
     * Returns the book by ID.
     * @param bookId        the book id.
     * @return              the book according to given ID.
     */
    public Book getBookById(int bookId) {
        BookDao bookDao = DaoFactory.getInstance().getBookDao();

        return bookDao.getBookById(bookId);
    }

    /**
     * Returns the retrived session factory object.
     * @return the session factory object.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Returns the current Session.
     * @return current session to use.
     */
    public Session getSession() {
        SessionFactory sessionFactory = getSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        return session;
    }

    public Session openTransaction() {
        Session session = getSession();
        session.beginTransaction();

        return session;
    }

    public void commitTransaction(final Session session) {
        session.getTransaction().commit();
    }

    private static Main startJndiServer() throws Exception {
        // Create an in-memory jndi
        NamingServer namingServer = new NamingServer();
        NamingContext.setLocal(namingServer);
        Main namingMain = new Main();
        namingMain.setInstallGlobalService(true);
        namingMain.setPort(-1);
        namingMain.start();
        return namingMain;
    }

    private static Context createJndiContext() throws Exception {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        return new InitialContext(props);
    }

    private static void bindTransactionManager() throws Exception {
        // as JBossTransactionManagerLookup extends JNDITransactionManagerLookup we must also register the TransactionManager
        bind("java:/TransactionManager", lookup.getTransactionManager(), lookup.getTransactionManager().getClass(), context);
    }

    private static void bindUserTransaction() throws Exception {
        // also the UserTransaction must be registered on jndi: org.hibernate.transaction.JTATransactionFactory#getUserTransaction() requires this
        bind("UserTransaction", lookup.getUserTransaction(), lookup.getUserTransaction().getClass(), context);
    }

    private static void bind(String jndiName, Object who, Class classType, Context ctx) throws Exception {
        // Ah ! This service isn't serializable, so we use a helper class
        NonSerializableFactory.bind(jndiName, who);
        Name n = ctx.getNameParser("").parse(jndiName);
        while (n.size() > 1) {
            String ctxName = n.get(0);
            try {
                ctx = (Context) ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                System.out.println("Creating subcontext:" + ctxName);
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        // The helper class NonSerializableFactory uses address type nns, we go on to
        // use the helper class to bind the service object in JNDI
        StringRefAddr addr = new StringRefAddr("nns", jndiName);
        Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);
        ctx.rebind(n.get(0), ref);
    }

}
