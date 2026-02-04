package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomHibernate extends GenericHibernate {

    // Peak hour multiplier
    private static final double PEAK_HOUR_MULTIPLIER = 1.5;

    // Peak hour definitions (lunch and dinner)
    private static final int LUNCH_START = 12;
    private static final int LUNCH_END = 14;
    private static final int DINNER_START = 18;
    private static final int DINNER_END = 21;

    public CustomHibernate(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    /**
     * Calculate dynamic price based on time of day (peak hours)
     */
    public double calculateDynamicPrice(double basePrice) {
        return calculateDynamicPrice(basePrice, LocalDateTime.now());
    }

    /**
     * Calculate dynamic price with specific time
     */
    public double calculateDynamicPrice(double basePrice, LocalDateTime orderTime) {
        if (isPeakHour(orderTime)) {
            return basePrice * PEAK_HOUR_MULTIPLIER;
        }
        return basePrice;
    }

    /**
     * Check if given time is during peak hours
     */
    private boolean isPeakHour(LocalDateTime time) {
        int hour = time.getHour();
        return (hour >= LUNCH_START && hour < LUNCH_END) ||
                (hour >= DINNER_START && hour < DINNER_END);
    }

    /**
     * Get user by login and password credentials
     */
    public User getUserByCredentials(String login, String psw) {
        User user = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query.select(root).where(
                    cb.and(
                            cb.equal(root.get("login"), login),
                            cb.equal(root.get("password"), psw)
                    )
            );

            Query q = entityManager.createQuery(query);
            user = (User) q.getSingleResult();
        } catch (Exception e) {
            System.err.println("User not found: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return user;
    }

    /**
     * Get all orders for a specific restaurant
     */
    public List<FoodOrder> getRestaurantOrders(Restaurant restaurant) {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);

            query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            Query q = entityManager.createQuery(query);
            orders = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching restaurant orders: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return orders;
    }

    /**
     * Get menu/cuisine for a specific restaurant
     */
    public List<Cuisine> getRestaurantCuisine(Restaurant restaurant) {
        List<Cuisine> menu = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Cuisine> query = cb.createQuery(Cuisine.class);
            Root<Cuisine> root = query.from(Cuisine.class);

            query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            Query q = entityManager.createQuery(query);
            menu = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching restaurant cuisine: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return menu;
    }

    /**
     * Get filtered restaurant orders with multiple criteria
     */
    public List<FoodOrder> getFilteredRestaurantOrders(
            OrderStatus orderStatus,
            BasicUser client,
            LocalDate startDate,
            LocalDate endDate,
            Restaurant restaurant) {

        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);

            List<Predicate> predicates = new ArrayList<>();

            // Filter by restaurant
            if (restaurant != null) {
                predicates.add(cb.equal(root.get("restaurant"), restaurant));
            }

            // Filter by order status
            if (orderStatus != null) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }

            // Filter by client
            if (client != null) {
                predicates.add(cb.equal(root.get("buyer"), client));
            }

            // Filter by start date
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateCreated"), startDate));
            }

            // Filter by end date
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateCreated"), endDate));
            }

            // Combine all predicates
            if (!predicates.isEmpty()) {
                query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
            } else {
                query.select(root);
            }

            Query q = entityManager.createQuery(query);
            orders = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error filtering orders: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return orders;
    }

    /**
     * Search users by multiple criteria
     */
    public List<User> searchUsers(String login, String firstName, String lastName, String role) {
        List<User> users = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            List<Predicate> predicates = new ArrayList<>();

            // Filter by login (partial match)
            if (login != null && !login.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("login")), "%" + login.toLowerCase() + "%"));
            }

            // Filter by first name (partial match)
            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + firstName.toLowerCase() + "%"));
            }

            // Filter by last name (partial match)
            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("surname")), "%" + lastName.toLowerCase() + "%"));
            }

            // Filter by role (exact match on entity type)
            if (role != null && !role.isEmpty() && !role.equals("All")) {
                predicates.add(cb.equal(root.type(), getClassForRole(role)));
            }

            // Combine all predicates
            if (!predicates.isEmpty()) {
                query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
            } else {
                query.select(root);
            }

            Query q = entityManager.createQuery(query);
            users = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return users;
    }

    /**
     * Helper method to convert role string to class
     */
    private Class<? extends User> getClassForRole(String role) {
        switch (role) {
            case "User":
                return User.class;
            case "BasicUser":
                return BasicUser.class;
            case "Restaurant":
                return Restaurant.class;
            case "Driver":
                return Driver.class;
            default:
                return User.class;
        }
    }

    /**
     * Get unclaimed orders (for driver marketplace)
     */
    public List<FoodOrder> getUnclaimedOrders() {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);

            // Orders that are placed or accepted but have no driver assigned
            Predicate noDriver = cb.isNull(root.get("driver"));
            Predicate statusPlaced = cb.equal(root.get("orderStatus"), OrderStatus.PLACED);
            Predicate statusAccepted = cb.equal(root.get("orderStatus"), OrderStatus.ACCEPTED);

            query.select(root).where(
                    cb.and(
                            noDriver,
                            cb.or(statusPlaced, statusAccepted)
                    )
            );

            Query q = entityManager.createQuery(query);
            orders = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching unclaimed orders: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return orders;
    }

    /**
     * Assign driver to order
     */
    public boolean assignDriverToOrder(FoodOrder order, Driver driver) {
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            FoodOrder managedOrder = entityManager.find(FoodOrder.class, order.getId());

            // Validate order status
            if (managedOrder.getOrderStatus() == OrderStatus.COMPLETED ||
                    managedOrder.getOrderStatus() == OrderStatus.DELIVERED) {
                System.err.println("Cannot assign driver to completed/delivered order");
                entityManager.getTransaction().rollback();
                return false;
            }

            managedOrder.setDriver(driver);
            managedOrder.setOrderStatus(OrderStatus.DRIVER_ASSIGNED);
            managedOrder.setDateUpdated(LocalDate.now());

            entityManager.merge(managedOrder);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error assigning driver: " + e.getMessage());
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    /**
     * Get all reviews for a specific user (as feedback recipient)
     */
    public List<Review> getUserReviews(BasicUser user) {
        List<Review> reviews = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Review> query = cb.createQuery(Review.class);
            Root<Review> root = query.from(Review.class);

            query.select(root).where(cb.equal(root.get("feedbackUser"), user));
            Query q = entityManager.createQuery(query);
            reviews = q.getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching user reviews: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return reviews;
    }

    /**
     * Calculate loyalty points for a customer based on order value
     */
    public int calculateLoyaltyPoints(double orderValue) {
        // 1 point per 10 euros spent
        return (int) (orderValue / 10);
    }

    /**
     * Add loyalty points to customer
     */
    public void addLoyaltyPoints(BasicUser customer, int points) {
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            BasicUser managedCustomer = entityManager.find(BasicUser.class, customer.getId());
            managedCustomer.setLoyaltyPoints(managedCustomer.getLoyaltyPoints() + points);

            entityManager.merge(managedCustomer);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error adding loyalty points: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }


    public void recalculateOrderPrice(FoodOrder order) {
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            FoodOrder managedOrder = entityManager.find(FoodOrder.class, order.getId());

            if (managedOrder.getCuisineList() != null && !managedOrder.getCuisineList().isEmpty()) {
                double basePrice = managedOrder.getCuisineList().stream()
                        .mapToDouble(Cuisine::getPrice)
                        .sum();

                // Apply dynamic pricing if order was created during peak hours
                LocalDateTime orderTime = managedOrder.getDateCreated() != null ?
                        managedOrder.getDateCreated().atStartOfDay() : LocalDateTime.now();
                double newPrice = calculateDynamicPrice(basePrice, orderTime);

                managedOrder.setPrice(newPrice);
                managedOrder.setDateUpdated(LocalDate.now());

                entityManager.merge(managedOrder);
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("Error recalculating order price: " + e.getMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }


    public boolean isChatLocked(Chat chat) {
        try {
            entityManager = entityManagerFactory.createEntityManager();
            Chat managedChat = entityManager.find(Chat.class, chat.getId());

            if (managedChat.getFoodOrder() != null) {
                return managedChat.getFoodOrder().getOrderStatus() == OrderStatus.COMPLETED;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking chat lock status: " + e.getMessage());
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
}