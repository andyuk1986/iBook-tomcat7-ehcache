package iBook.dao.impl.hibernate;

import iBook.dao.BookDao;
import iBook.domain.Author;
import iBook.domain.Book;
import iBook.domain.Category;
import iBook.utils.Utils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Map;

/**
 * Book Dao Hibernate Implementation..
 */
public class BookDaoImpl implements BookDao {
	public static final int COVER_BOOK_COUNT = 4;


    @Override
	public Book getBookById(int id) {
        Session session = Utils.getInstance().openTransaction();
        Object book = session.get(Book.class, id);

        Utils.getInstance().commitTransaction(session);

        return (book != null ? (Book) book : null);
	}

    @Override
	public List<Book> getBooksByAuthorId(Author author) {
        Session session = Utils.getInstance().openTransaction();

		Query query = session.getNamedQuery("listByAuthor");
		query.setParameter("author", author);

		List<Book> books = query.list();

        Utils.getInstance().commitTransaction(session);

        return books;
	}

    @Override
	public List<Book> getBooksByCategory(Category category) {
        Session session = Utils.getInstance().openTransaction();

        Query query = session.getNamedQuery("listByCategory");
		query.setParameter("category", category);

		List<Book> books = query.list();

        Utils.getInstance().commitTransaction(session);

        return books;
	}

    @Override
	public List<Book> getAllBooks() {
		return getBooksByQuery("listAllBook", 0);
	}

    @Override
	public List<Book> getBestSellers() {
		return getBooksByQuery("listBestSellers", 0);
	}

    @Override
	public List<Book> getCoverBooks() {
		return getBooksByQuery("listRandomBooks", COVER_BOOK_COUNT);
	}

    @Override
	public List<Book> getBooksByCriterias(Map<String, String> criteria) {
        Session session = Utils.getInstance().openTransaction();

        Criteria criteriaObj = session.createCriteria(Book.class);
        for(Map.Entry<String, String> cr : criteria.entrySet()) {
            criteriaObj.add(Restrictions.like(cr.getKey(), cr.getValue()));
        }

        List resultList = criteriaObj.setCacheable(true).list();
        Utils.getInstance().commitTransaction(session);

        return (List<Book>) resultList;
	}
	
	private List<Book> getBooksByQuery(final String namedQueryName, final int limit) {
        Session session = Utils.getInstance().openTransaction();

		Query query = session.getNamedQuery(namedQueryName);
		if(limit > 0) {
			query.setMaxResults(limit);
		}

		List<Book> books = query.list();

        Utils.getInstance().commitTransaction(session);

        return books;
	}

}
