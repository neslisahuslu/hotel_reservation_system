# 🏨 Hotel Reservation System — Java 21 (Spring Boot Microservices)

Bu proje, otel, oda ve rezervasyon yönetimi yapan mikroservis tabanlı bir sistemdir.  
JWT tabanlı kimlik doğrulama ile kullanıcılar giriş yapar ve rollerine göre (ADMIN / USER) erişim sağlar.

---

## 🚀 Kullanılan Teknolojiler
- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Cloud Gateway**
- **Spring Security (JWT Authentication)**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Kafka (event-driven iletişim için)**

---

## 🧩 Servisler ve Portlar

| Servis                   | Açıklama                              | Port |
|--------------------------|---------------------------------------|------|
| **API Gateway**          | İsteklerin yönlendirildiği ana katman | `8080` |
| **Hotel Service**        | Otel ve Oda CRUD işlemleri            | `8081` |
| **Reservation Service**  | Rezervasyon işlemleri                 | `8082` |
| **Notification Service** | Notifikasyon işlemleri                | `8083` |
| **Auth Service**         | JWT tabanlı kimlik doğrulama          | `8084` |

---

## ⚙️ Projeyi Çalıştırma

Projeyi Docker ile başlatmak için:
```bash
docker compose build
docker compose up -d
```

### Servis URL’leri
- Gateway: [http://localhost:8080](http://localhost:8080)
- Hotel Service: [http://localhost:8081](http://localhost:8081)
- Reservation Service: [http://localhost:8082](http://localhost:8082)
- Notification Service: [http://localhost:8083](http://localhost:8083)
- Auth Service: [http://localhost:8084](http://localhost:8084)

---

## 🔐 Kimlik Doğrulama (Auth Service)

### Endpoint
`POST /api/auth/login`

Kullanıcı girişi yapar ve JWT token döner.

#### Örnek Request:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

#### Örnek Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
  "username": "admin",
  "roles": ["ADMIN"]
}
```

---

## 👥 Varsayılan Kullanıcılar

Sistem başlangıcında iki kullanıcı mevcuttur:

| Rol | Username | Password |
|-----|-----------|-----------|
| 🛠️ Admin | `admin` | `admin123` |
| 👤 User | `user` | `user123` |

Token aldıktan sonra diğer servislerin endpointlerine istek atabilirsiniz.

---

## 📘 API Endpointleri

### 🏨 Hotel API (`/api/hotels`)
| HTTP | Endpoint | Açıklama | Yetki |
|------|-----------|-----------|--------|
| `POST` | `/api/hotels` | Yeni otel oluşturur | ADMIN |
| `PUT` | `/api/hotels/{hotelId}` | Otel günceller | ADMIN |
| `DELETE` | `/api/hotels` | Otel siler | ADMIN |
| `GET` | `/api/hotels/{hotelId}` | Otel getirir | ADMIN, USER |
| `GET` | `/api/hotels` | Otelleri listeler | ADMIN, USER |

---

### 🏠 Room API (`/api/rooms`)
| HTTP | Endpoint | Açıklama | Yetki |
|------|-----------|-----------|--------|
| `POST` | `/api/rooms` | Yeni oda oluşturur | ADMIN |
| `PUT` | `/api/rooms/{roomId}` | Oda günceller | ADMIN |
| `DELETE` | `/api/rooms` | Oda siler | ADMIN |
| `GET` | `/api/rooms/{roomId}` | Oda getirir | ADMIN, USER |
| `GET` | `/api/rooms` | Odaları listeler | ADMIN, USER |

---

### 🧾 Reservation API (`/api/reservations`)
| HTTP | Endpoint | Açıklama | Yetki       |
|------|-----------|-----------|-------------|
| `POST` | `/api/reservations?email={email}` | Yeni rezervasyon oluşturur | USER, ADMIN |
| `GET` | `/api/reservations` | Kullanıcının kendi rezervasyonlarını listeler | USER, ADMIN |

#### Örnek `POST /api/reservations` Request:
```json
{
  "hotelId": "uuid-hotel-id",
  "roomId": "uuid-room-id",
  "userId": "uuid-user-id",
  "guestName": "Admin",
  "checkInDate": "2025-10-21",
  "checkOutDate": "2025-10-24"
}
```

---

## 🧱 Katman Yapısı

```
├── api-gateway
├── auth-service
├── hotel-service
├── room-service
├── reservation-service
└── common
```

---

## 💡 Örnek Akış

1. `/api/auth/login` ile giriş yap → JWT token al.
2. Token’ı Authorization header’a ekle.
3. `/api/hotels`, `/api/rooms`, `/api/reservations` endpointlerini rolüne göre kullan.

---

## 🧾 Lisans

Bu proje eğitim ve demo amaçlı olarak geliştirilmiştir.  
© 2025 — Hotel Reservation System
