# 🛒 E-Commerce Backend

RESTful API cho hệ thống thương mại điện tử xây dựng bằng Spring Boot 4.x

---

## 🚀 Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Spring Boot 4.x (Java 21) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Documentation | Swagger (OpenAPI) |
| Build tool | Maven |

---

## ⚙️ Cài đặt & Chạy

### Yêu cầu
- Java 21+
- PostgreSQL
- Maven

### Cấu hình

1. Clone repository:
```bash
git clone https://github.com/yourusername/ecommerce-backend.git
cd ecommerce-backend
```

2. Tạo database PostgreSQL:
```sql
CREATE DATABASE ecommerce_db;
```

3. Copy file cấu hình mẫu và điền thông tin:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

4. Chỉnh sửa `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your-secret-key-at-least-32-characters
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

5. Chạy ứng dụng:
```bash
mvn spring-boot:run
```

---

## 👤 Tài khoản mặc định

Khi khởi động lần đầu, hệ thống tự động tạo tài khoản Admin:

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |

---

## 📁 Cấu trúc project

```
src/main/java/com/example/
├── config/              # SecurityConfig
├── controller/          # REST Controllers
├── service/             # Business Logic
├── repository/          # JPA Repositories
├── entity/              # Database Entities
├── dto/
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── security/            # JWT Filter, UserDetailsService
├── exception/           # Custom Exceptions
└── DataInitializer.java # Tạo tài khoản Admin mặc định
```

---

## 🗄️ Database Schema

```
users
├── id
├── username (unique)
├── password (BCrypt)
└── role (USER / ADMIN)

user_profile
├── id
├── user_id (FK → users)
├── full_name
├── phone_number
├── email
└── address

refresh_token
├── id
├── token (UUID)
├── expiry_date
└── user_id (FK → users)
```

---

## 📌 API Endpoints

### 🔐 Auth `/api/auth`

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Đăng ký tài khoản | Public |
| POST | `/api/auth/login` | Đăng nhập | Public |
| POST | `/api/auth/refresh-token` | Làm mới Access Token | Public |
| POST | `/api/auth/logout` | Đăng xuất | USER |

#### Register
```json
POST /api/auth/register
{
  "username": "john",
  "password": "123456"
}
```

#### Login
```json
POST /api/auth/login
{
  "username": "john",
  "password": "123456"
}

// Response
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "uuid-string",
  "username": "john",
  "role": "USER"
}
```

#### Refresh Token
```json
POST /api/auth/refresh-token
{
  "refreshToken": "uuid-string"
}
```

---

### 👤 User `/api/user`

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/user/profile` | Xem profile | USER |
| PUT | `/api/user/profile` | Cập nhật profile | USER |
| PUT | `/api/user/change-password` | Đổi mật khẩu | USER |

#### Xem profile
```
GET /api/user/profile
Authorization: Bearer <accessToken>

// Response
{
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789",
  "email": "vana@gmail.com",
  "address": "123 Nguyen Hue, Q1, HCM"
}
```

#### Cập nhật profile
```json
PUT /api/user/profile
Authorization: Bearer <accessToken>

{
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789",
  "email": "vana@gmail.com",
  "address": "123 Nguyen Hue, Q1, HCM"
}
```

#### Đổi mật khẩu
```json
PUT /api/user/change-password
Authorization: Bearer <accessToken>

{
  "oldPassword": "123456",
  "newPassword": "newpass123"
}
```

---

### 🛡️ Admin `/api/admin`

> Yêu cầu tài khoản có role **ADMIN**

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/admin/users` | Danh sách tất cả user (phân trang) | ADMIN |
| GET | `/api/admin/users/{id}` | Xem chi tiết 1 user | ADMIN |
| DELETE | `/api/admin/users/{id}` | Xóa user | ADMIN |

#### Danh sách user
```
GET /api/admin/users?page=0&size=10
Authorization: Bearer <accessToken ADMIN>
```

#### Xóa user
```
DELETE /api/admin/users/1
Authorization: Bearer <accessToken ADMIN>

// Response
{
  "message": "Xóa tài khoản thành công"
}
```

---

## 🔐 Cơ chế bảo mật

### JWT Flow
```
Login → Access Token (1 ngày) + Refresh Token (7 ngày)
     ↓
Mọi request gửi kèm: Authorization: Bearer <accessToken>
     ↓
Access Token hết hạn → Dùng Refresh Token để lấy token mới
     ↓
Refresh Token hết hạn → Đăng nhập lại
```

### Phân quyền
```
/api/auth/**     → Public
/api/user/**     → USER + ADMIN
/api/admin/**    → ADMIN only
```

---

## 📄 Swagger UI

Sau khi chạy ứng dụng, truy cập:
```
http://localhost:8080/swagger-ui.html
```

---

## 🗺️ Roadmap

- [x] Authentication (Register, Login, Logout, JWT, Refresh Token)
- [x] User Profile (Xem, cập nhật, đổi mật khẩu)
- [x] Admin quản lý User
- [ ] Category
- [ ] Product
- [ ] Cart
- [ ] Order
- [ ] Payment
