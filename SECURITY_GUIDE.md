# KoraStudy - Hướng dẫn Bảo mật API Keys

## ⚠️ QUAN TRỌNG: Đã xóa thông tin nhạy cảm khỏi Git

File application.properties ban đầu chứa nhiều thông tin nhạy cảm. Các file đã được cấu hình lại để bảo mật.

## 📁 Các file đã tạo

### Backend (Spring Boot)
- `application.properties` - File cấu hình chính (chỉ chứa biến môi trường)
- `application-local.properties` - File chứa giá trị thực (KHÔNG commit lên Git)
- `application.properties.example` - File mẫu hướng dẫn

### Frontend
- `.env` - File chứa biến môi trường (KHÔNG commit lên Git)
- `.env.example` - File mẫu hướng dẫn

## 🔧 Cách sử dụng

### 1. Backend Development

Khi chạy local, Spring Boot sẽ tự động load file `application-local.properties`:

```bash
# Chạy với profile local
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Hoặc thiết lập biến môi trường
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

**Hoặc** thiết lập biến môi trường trong IDE (IntelliJ IDEA):
- Run → Edit Configurations
- Environment variables: `DB_USERNAME=sa;DB_PASSWORD=123456;JWT_SECRET=...`

### 2. Frontend Development

Copy file `.env.example` thành `.env` và điền giá trị:

```bash
cd korastudy-fe
cp .env.example .env
# Chỉnh sửa .env với giá trị thực của bạn
```

### 3. Production Deployment

**Không dùng file properties!** Thiết lập biến môi trường trực tiếp:

```bash
# Azure App Service / Cloud
DB_URL=jdbc:sqlserver://...
DB_USERNAME=admin
DB_PASSWORD=secure-password
JWT_SECRET=production-secret-key
CLOUDINARY_API_KEY=...
GEMINI_API_KEY=...
```

## 🔐 Các biến môi trường cần thiết

### Database
- `DB_URL` - Connection string
- `DB_USERNAME` - Database username  
- `DB_PASSWORD` - Database password

### JWT
- `JWT_SECRET` - Secret key (ít nhất 256 bits)
- `JWT_EXPIRATION` - Token expiration time

### Cloudinary (Image Storage)
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

### VNPay (Payment)
- `VNPAY_TMN_CODE`
- `VNPAY_HASH_SECRET`

### Email (SMTP)
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

### Gemini AI
- `GEMINI_API_KEY`

## 📝 Trước khi commit lên Git

**QUAN TRỌNG**: Đảm bảo các file sau KHÔNG được commit:

```bash
# Kiểm tra git status
git status

# Các file này KHÔNG được xuất hiện:
# - application-local.properties
# - .env
# - *.env.local
```

## 🔄 Nếu đã push nhầm API keys lên Git

**Phải làm ngay:**

1. **Thay đổi tất cả API keys và passwords**
   - Database password
   - JWT secret
   - Cloudinary credentials
   - VNPay credentials
   - Email password
   - Gemini API key

2. **Xóa lịch sử Git (Nếu cần thiết)**

```bash
# Cách 1: Sử dụng BFG Repo-Cleaner
java -jar bfg.jar --delete-files application.properties
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Cách 2: Filter branch
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch src/main/resources/application-local.properties" \
  --prune-empty --tag-name-filter cat -- --all

# Force push (cẩn thận!)
git push origin --force --all
```

3. **Rotate keys từ các dịch vụ:**
   - Cloudinary: [Dashboard](https://cloudinary.com/console)
   - Gemini: [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Gmail: Tạo App Password mới

## ✅ Checklist trước khi commit

- [ ] Đã xóa tất cả API keys khỏi file được commit
- [ ] Đã thêm các file sensitive vào .gitignore
- [ ] Đã tạo file .env.example / application.properties.example
- [ ] Đã test với biến môi trường
- [ ] Đã review `git diff` trước khi commit

## 📚 Tài liệu tham khảo

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Vite Environment Variables](https://vitejs.dev/guide/env-and-mode.html)
- [Git Remove Sensitive Data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
