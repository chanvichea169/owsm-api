# Hospital Management System Backend

This project is the backend for a Hospital Management System, built with Spring Boot.

## Recent Changes: User Role Management & Flexible Login

This update introduces a more robust way to handle user roles and enhances the login functionality to allow authentication using either username or email.

### User Role Management Changes:

-   **`RoleName` Enumeration:** A new `RoleName` enum (`ADMIN`, `USER`) has been introduced in `enumeration.com.owsm.AuthService.RoleName` for type-safe role definitions.
-   **`Role` Entity:** The `Role` entity (`model.com.owsm.AuthService.Role`) now uses the `RoleName` enum for its `name` field and is mapped to the `roles` table.
-   **`data.sql`:** The `src/main/resources/data.sql` file now automatically inserts `ADMIN` and `USER` roles into the `roles` table on application startup.
-   **`UserRequest` DTO:** The `UserRequest` DTO (`dto.com.owsm.AuthService.UserRequest`) now accepts a `roleId` (Long) instead of a `role` (String) when creating or updating a user.
-   **`UserServiceImpl`:** The `registerUser` and `updateUser` methods in `serviceImpl.service.com.owsm.AuthService.UserServiceImpl` have been updated to use the `roleId` from `UserRequest` to fetch and assign the corresponding `Role` object.
-   **`UserHandlerService`:** The `convertToUser` method in `handler.service.com.owsm.AuthService.UserHandlerService` has been updated to use `roleId` for role assignment, and `convertToUserResponse` now correctly converts the `RoleName` enum to its string representation.

### Flexible Login Changes:

-   **`UserRepository`:** A new `findByUsername(String username)` method has been added to `repository.com.owsm.AuthService.UserRepository` to allow fetching users by their username.
-   **`UserDetailsServiceImpl`:** The `loadUserByUsername` method in `serviceImpl.service.com.owsm.AuthService.UserDetailsServiceImpl` has been modified to first attempt to find a user by email, and if not found, then by username. This enables users to log in using either their registered email or username.

## How to Test

### 1. Ensure Roles are Inserted

Upon application startup, the `data.sql` script will automatically insert the `ADMIN` and `USER` roles into the `roles` table. You can verify this by checking your database.

-   `ADMIN` role will have `id = 1`
-   `USER` role will have `id = 2`

### 2. Register a New User with a Role

**Endpoint:** `POST /api/users/register`

**Request Body (JSON):**

To register a user as an `ADMIN`:

```json
{
    "username": "adminuser",
    "email": "admin@example.com",
    "password": "password123",
    "roleId": 1
}
```

To register a user as a `USER`:

```json
{
    "username": "normaluser",
    "email": "user@example.com",
    "password": "password123",
    "roleId": 2
}
```

**Expected Response:**

A successful registration will return a `200 OK` status with the `UserResponse` object, including the assigned role.

### 3. Update an Existing User's Details (including password and role)

**Endpoint:** `PUT /api/users/{id}`

**Request Body (JSON):**

To update a user's details, including changing their password and role (assuming user ID is 1):

```json
{
    "username": "updateduser",
    "email": "updated@example.com",
    "password": "newStrongPassword123",
    "roleId": 1 
}
```

**Expected Response:**

A successful update will return a `200 OK` status with the updated `UserResponse` object.

### 4. Login with Username or Email

**Endpoint:** `POST /api/users/login`

**Request Body (JSON):**

To login with email:

```json
{
    "email": "admin@example.com",
    "password": "password123"
}
```

To login with username:

```json
{
    "username": "adminuser",
    "password": "password123"
}
```

**Expected Response:**

A successful login will return a `200 OK` status with the `UserResponse` object, including a JWT token.

### 5. Verify OTP

**Endpoint:** `POST /api/users/verify-otp`

**Request Body (JSON):**

```json
{
    "email": "user@example.com",
    "otp": "123456" 
}
```

**Expected Response:**

A successful OTP verification will return a `200 OK` status with the `UserResponse` object, including a JWT token.

### 6. Resend OTP

**Endpoint:** `POST /api/users/resend-otp`

**Request Body (JSON):**

```json
{
    "email": "user@example.com"
}
```

**Expected Response:**

A successful OTP resend will return a `200 OK` status with the message "OTP resent successfully".

### 7. Delete a User

**Endpoint:** `DELETE /api/users/{id}`

**Path Variable:** `{id}` - The ID of the user to delete.

**Example:** `DELETE /api/users/1` (to delete user with ID 1)

**Expected Response:**

A successful deletion will return a `204 No Content` status.

---

This README will be further expanded with more details about other functionalities and setup instructions.