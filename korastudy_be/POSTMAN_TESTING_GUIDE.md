# Hướng dẫn Test API với Postman

## 🚀 Cách thêm dữ liệu mẫu và test Frontend

### 1. Khởi động Backend
```bash
cd "c:\Users\ADMIN\Documents\graduation project\KoraStudy_BE\korastudy_be"
mvn spring-boot:run
```

### 2. Thêm dữ liệu mẫu thông qua API

#### 🔧 Tạo dữ liệu mẫu Mock Test
**Endpoint:** `POST http://localhost:8080/api/v1/admin/sample-data/mock-tests`
**Headers:**
```
Content-Type: application/json
```

**Body:** Không cần body

---

#### 📋 Danh sách API để test Frontend

### 1. Lấy tất cả Mock Tests
**Method:** `GET`
**URL:** `http://localhost:8080/api/v1/exams`
**Query Parameters:**
- `page=0`
- `size=10`
- `sortBy=createdAt`
- `sortDirection=desc`

**Example URL:** `http://localhost:8080/api/v1/exams?page=0&size=10&sortBy=createdAt&sortDirection=desc`

### 2. Tìm kiếm Mock Tests
**Method:** `POST`
**URL:** `http://localhost:8080/api/v1/exams/search`
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "title": "TOPIK",
  "level": "TOPIK I",
  "page": 0,
  "size": 10
}
```

### 3. Lấy Mock Test để làm bài (không có đáp án)
**Method:** `GET`
**URL:** `http://localhost:8080/api/v1/exams/{id}`
**Example:** `http://localhost:8080/api/v1/exams/1`

### 4. Lấy Mock Tests phổ biến
**Method:** `GET`
**URL:** `http://localhost:8080/api/v1/exams/popular`
**Query Parameters:**
- `limit=5`

**Example URL:** `http://localhost:8080/api/v1/exams/popular?limit=5`

### 5. Health Check
**Method:** `GET`
**URL:** `http://localhost:8080/api/v1/exams/health`

---

## 🧪 Các bước test Frontend

### Bước 1: Tạo dữ liệu mẫu
1. Mở Postman
2. Tạo request POST: `http://localhost:8080/api/v1/admin/sample-data/mock-tests`
3. Gửi request
4. Kiểm tra response thành công

### Bước 2: Test API endpoints
1. **Test GET all exams:**
   - URL: `http://localhost:8080/api/v1/exams`
   - Kiểm tra có trả về danh sách mock tests

2. **Test GET specific exam:**
   - URL: `http://localhost:8080/api/v1/exams/1`
   - Kiểm tra có trả về chi tiết exam với questions

3. **Test Search:**
   - URL: `http://localhost:8080/api/v1/exams/search`
   - Body: `{"title": "TOPIK"}`
   - Kiểm tra có filter đúng

### Bước 3: Test Frontend
1. Khởi động Frontend:
   ```bash
   cd "c:\Users\ADMIN\Documents\graduation project\KoraStudy_FE\korastudy-fe"
   npm run dev
   ```

2. Truy cập: `http://localhost:3000/de-thi`

3. Kiểm tra:
   - Danh sách exams có hiển thị
   - Có thể click vào exam để xem chi tiết
   - Search functionality hoạt động

---

## 📝 Sample Data được tạo

### Mock Tests:
1. **TOPIK I - Test 1** (40 câu, 2 phần, 100 phút)
2. **TOPIK I - Test 2** (40 câu, 2 phần, 100 phút)
3. **TOPIK II - Test 1** (50 câu, 3 phần, 180 phút)
4. **TOPIK Beginner** (30 câu, 2 phần, 90 phút)

### Mỗi Mock Test có:
- **Parts:** Listening, Reading, (Writing cho TOPIK II)
- **Questions:** Câu hỏi mẫu với audio/image URLs
- **Answers:** 4 lựa chọn cho mỗi câu, có đáp án đúng

---

## 🔍 Troubleshooting

### Lỗi thường gặp:

1. **Database connection error:**
   - Kiểm tra MySQL có đang chạy
   - Kiểm tra config trong `application.properties`

2. **Port 8080 đã được sử dụng:**
   - Đổi port trong `application.properties`: `server.port=8081`
   - Update frontend API URL tương ứng

3. **CORS errors:**
   - Đã config `@CrossOrigin` trong controllers
   - Kiểm tra frontend đang call đúng URL

4. **Frontend không load data:**
   - Kiểm tra environment variables: `VITE_API_URL`
   - Kiểm tra Network tab trong Developer Tools
   - Verify backend API trả về data đúng format

---

## 🎯 Expected Results

Sau khi làm theo hướng dẫn:

1. ✅ Backend API hoạt động và trả về sample data
2. ✅ Frontend có thể fetch và hiển thị danh sách exams
3. ✅ Search và filter functionality hoạt động
4. ✅ Exam detail pages hiển thị đúng nội dung
5. ✅ Không có CORS errors hay network issues

---

## 📞 Next Steps

Nếu tất cả hoạt động tốt, bạn có thể:

1. **Test exam taking flow:** Thêm authentication và test submit answers
2. **Test user features:** Tạo user accounts và test exam history
3. **Performance testing:** Test với nhiều data hơn
4. **UI/UX improvements:** Cải thiện giao diện frontend
