package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory ;

    public PlayerRepositoryDB() {
    Properties properties = new Properties();
    properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
    properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
    properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
    properties.put(Environment.USER, "root");
    properties.put(Environment.PASS, "root");
    properties.put(Environment.HBM2DDL_AUTO, "update");
    properties.put(Environment.AUTOCOMMIT, false);
//    properties.put(Environment.GENERATE_STATISTICS, true);
    properties.put(Environment.SHOW_SQL,true);
    properties.put(Environment.FORMAT_SQL,true);

    sessionFactory = new Configuration()
            .addAnnotatedClass(Player.class)
            .addProperties(properties)
            .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery("select * from rpg.player", Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()){
            Query<Long> namedQuery = session.createNamedQuery("player_getAllCount", Long.class);
            return  Math.toIntExact(namedQuery.uniqueResult());
        }
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.update(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
//            Player player = session.find(Player.class, id);
//            return Optional.of(player);
            return Optional.ofNullable(session.get(Player.class, id));
        }
    }

    @Override
    public void delete(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void beforeStop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

}