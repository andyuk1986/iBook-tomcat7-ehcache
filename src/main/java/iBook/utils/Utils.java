package iBook.utils;

import iBook.dao.BookDao;
import iBook.dao.factory.DaoFactory;
import iBook.dao.statefull.BookWishList;
import iBook.dao.statefull.BookWishListBean;
import iBook.domain.Book;
import iBook.domain.User;
import iBook.web.Page;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;

/**
 * Utility class for working with books and wishlist.
 */
public final class Utils {
    private static Utils instance = null;
    private static InitialContext ctx = null;
    private static Context context = null;
    private static final SessionFactory sessionFactory;

    static {
        try {
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

    /**
     * Returns the wishlist object.
     * @return      the wishlist statefull session bean.
     */
    public BookWishList getBookWishList(HttpSession session) {
        BookWishList wishList = null;
        CacheManager cacheManager = CacheManager.getInstance();

        User user = (User) session.getAttribute(Page.LOGGED_IN_USER);
        Element element = cacheManager.getCache("sfsb").get("wishList" + user.getId());
        if (element == null) {

            wishList = new BookWishListBean();
            wishList.init(user);

            element = new Element("wishList" + user.getId(), wishList);
            cacheManager.getCache("sfsb").put(element);
        } else {
            wishList = (BookWishList) element.getValue();
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
       System.out.println("test commit");
        return session;
    }

    public void commitTransaction(final Session session) {
        session.getTransaction().commit();
    }
}
