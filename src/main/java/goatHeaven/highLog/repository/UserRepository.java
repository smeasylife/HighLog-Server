package goatHeaven.highLog.repository;

import goatHeaven.highLog.jooq.tables.JUsers;
import goatHeaven.highLog.jooq.tables.daos.UsersDao;
import goatHeaven.highLog.jooq.tables.pojos.Users;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DSLContext dsl;
    private final UsersDao dao;
    private static final JUsers USERS = JUsers.USERS;

    public UserRepository(Configuration configuration, DSLContext dsl) {
        this.dao = new UsersDao(configuration);
        this.dsl = dsl;
    }

    public Optional<Users> findById(Long id) {
        return dao.fetchOptionalById(id);
    }

    public Optional<Users> findByEmail(String email) {
        return dao.fetchOptionalByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return dsl.fetchExists(
                dsl.selectFrom(USERS)
                        .where(USERS.EMAIL.eq(email))
        );
    }

    public Users insert(Users user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        dao.insert(user);
        return user;
    }

    public void updatePassword(Long userId, String encodedPassword) {
        dsl.update(USERS)
                .set(USERS.PASSWORD, encodedPassword)
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(userId))
                .execute();
    }

    public void deleteById(Long userId) {
        dsl.deleteFrom(USERS)
                .where(USERS.ID.eq(userId))
                .execute();
    }
}
