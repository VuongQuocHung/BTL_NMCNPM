package util;

import model.dto.UserResponse;
import dao.OrderDao;
import dao.ReviewDao;
import dao.UserDao;
import model.User;
import util.PasswordUtil;
import util.ApiException;
import java.util.List;

public class UserService {
    private final UserDao userDao;
    private final OrderDao orderDao;
    private final ReviewDao reviewDao;

    public UserService(UserDao userDao, OrderDao orderDao, ReviewDao reviewDao) {
        this.userDao = userDao; this.orderDao = orderDao; this.reviewDao = reviewDao;
    }

    public List<User> getFilteredUsers(String email, String fullName, String phone, Long roleId,
                                        int page, int size, String sortBy, String sortDir) {
        return userDao.findFiltered(email, fullName, phone, roleId, page, size, sortBy, sortDir);
    }

    public User getUserById(Long id) {
        return userDao.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    public User createUser(User user) {
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        return userDao.save(user);
    }

    public User updateUser(Long id, User details) {
        User user = getUserById(id);
        user.setFullName(details.getFullName());
        user.setPhone(details.getPhone());
        if (details.getRole() != null) user.setRole(details.getRole());
        return userDao.merge(user);
    }

    public void deleteUser(Long id) { userDao.delete(getUserById(id)); }

    public UserResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        return UserResponse.builder()
                .id(user.getId()).email(user.getEmail()).fullName(user.getFullName())
                .phone(user.getPhone()).roleName(user.getRole() != null ? user.getRole().getName() : "").build();
    }
}
