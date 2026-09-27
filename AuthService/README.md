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

## User Location Mapping

Registration and user updates accept an optional `streetAddress` and `villageCode`.
The Auth Service resolves the village and returns a `location` object containing
the village, commune, district, and province codes and names.

To replace a user's location:

**Endpoint:** `PUT /api/users/{id}/location`

```json
{
  "streetAddress": "House 10, Street 5",
  "villageCode": "12030501"
}
```

Use `GET /api/users/{id}/location` to retrieve a user's location. Users can also
be filtered with `GET /api/users/by-village/{villageCode}`,
`/api/users/by-commune/{communeCode}`, `/api/users/by-district/{districtCode}`,
and `/api/users/by-province/{provinceCode}`.

The user response includes the street address and mapped location, for example:

```json
{
  "streetAddress": "House 10, Street 5",
  "location": {
    "villageCode": "12030501",
    "villageEn": "Village name",
    "communeCode": 120305,
    "districtCode": 1203,
    "provinceCode": 12
  }
}
```

## Administrative Location CRUD

The administrative location API supports create, read, update, and delete
operations for provinces, districts, communes, and villages. Create children
only after creating their parent. Reads are public so registration forms can
populate location dropdowns before login. Writes require an `ADMIN` or
`HEAD_OF_DEPARTMENT` authority.

| Resource | Collection | Parent field |
| --- | --- | --- |
| Province | `/api/locations/provinces` | — |
| District | `/api/locations/districts` | `provinceCode` |
| Commune | `/api/locations/communes` | `districtCode` |
| Village | `/api/locations/villages` | `communeCode` |

Use `POST` on a collection to create, `GET` on a collection to list, `GET
/{code}` to fetch one, `PUT /{code}` to update, and `DELETE /{code}` to remove
an item. District, commune, and village collection queries return all records
by default and can be filtered by parent, for example
`GET /api/locations/districts?provinceCode=12`.

Example village create request:

```json
{
  "villageCode": "12030501",
  "villageKh": "ភូមិថ្មី",
  "villageEn": "New Village",
  "communeCode": 120305
}
```

Province, district, and commune codes use integers to match their database
columns. Village codes remain strings to preserve leading zeroes.

Updates retain the existing code; the request code must match the code in the
URL. A location with child records (or a village assigned to users) cannot be
deleted.

### Cascading province, district, commune, and village selection

The API supports dependent dropdowns. Populate the next dropdown after the
current selection by passing the selected code:

1. Load province options with `GET /api/locations/provinces`.
2. After selecting a province, load its districts with
   `GET /api/locations/provinces/{provinceCode}/districts`.
3. After selecting a district, load its communes with
   `GET /api/locations/districts/{districtCode}/communes`.
4. After selecting a commune, load its villages with
   `GET /api/locations/communes/{communeCode}/villages`.

Each response item includes `code`, `nameEn`, and `nameKh` for the dropdown
label, plus `parentCode`. Use `code` as the selected value and clear all
downstream selections whenever a parent selection changes.

The API gateway routes `/api/locations/**` to AuthService and is the single
CORS owner for browser requests. It allows `http://localhost:3000` by default.
Override the gateway's comma-separated allowlist with `CORS_ALLOWED_ORIGINS`
when deploying the frontend at a different origin. AuthService does not add
CORS headers, preventing duplicate `Access-Control-Allow-Origin` response
headers.

---

This README will be further expanded with more details about other functionalities and setup instructions.