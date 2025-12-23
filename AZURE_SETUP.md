# 🔧 Hướng dẫn Cấu hình Azure Services

## 📋 Yêu cầu
- Tài khoản Azure Student (đã kích hoạt)
- Java Backend đã chạy
- Frontend (User + Admin) đã cài dependencies

## 1️⃣ Tạo Azure Translator Resource

### Bước 1: Truy cập Azure Portal
1. Đăng nhập https://portal.azure.com
2. Tìm kiếm "Translator" 
3. Click **Create**

### Bước 2: Điền thông tin
- **Subscription**: Azure for Students
- **Resource Group**: Tạo mới hoặc chọn có sẵn (ví dụ: `korastudy-rg`)
- **Region**: **East Asia** (gần Việt Nam nhất)
- **Name**: `korastudy-translator` (hoặc tên khác)
- **Pricing tier**: **Free F0** (2M ký tự miễn phí/tháng)

### Bước 3: Lấy Key và Region
1. Sau khi tạo xong, vào resource
2. Chọn **Keys and Endpoint** ở menu bên trái
3. Copy:
   - **KEY 1** → `azure.translator.key`
   - **Location/Region** → `azure.translator.region`

---

## 2️⃣ Tạo Azure Text-to-Speech Resource

### Bước 1: Tạo Speech Service
1. Tìm kiếm "Speech Services"
2. Click **Create**

### Bước 2: Điền thông tin
- **Subscription**: Azure for Students
- **Resource Group**: Dùng chung với Translator (`korastudy-rg`)
- **Region**: **East Asia**
- **Name**: `korastudy-speech` (hoặc tên khác)
- **Pricing tier**: **Free F0** (5 triệu ký tự TTS miễn phí/tháng)

### Bước 3: Lấy Key và Region
1. Vào resource vừa tạo
2. Chọn **Keys and Endpoint**
3. Copy:
   - **KEY 1** → `azure.speech.key`
   - **Location/Region** → `azure.speech.region`

---

## 3️⃣ Cấu hình Backend (Spring Boot)

### File: `application-local.properties`

```properties
# Azure Translator Configuration
azure.translator.key=YOUR_TRANSLATOR_KEY_HERE
azure.translator.region=eastasia
azure.translator.endpoint=https://api.cognitive.microsofttranslator.com

# Azure Text-to-Speech Configuration
azure.speech.key=YOUR_SPEECH_KEY_HERE
azure.speech.region=eastasia
```

⚠️ **Lưu ý**: 
- Thay `YOUR_TRANSLATOR_KEY_HERE` bằng KEY 1 từ Translator
- Thay `YOUR_SPEECH_KEY_HERE` bằng KEY 1 từ Speech Service
- **KHÔNG** commit file này lên Git!

---

## 4️⃣ Kiểm tra Backend

### Test Translator API
```bash
curl -X POST http://localhost:8080/api/v1/azure/translate/ko-to-vi \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"text":"안녕하세요"}'
```

**Kết quả mong đợi:**
```json
{
  "success": true,
  "originalText": "안녕하세요",
  "translatedText": "Xin chào",
  "fromLanguage": "ko",
  "toLanguage": "vi"
}
```

### Test Text-to-Speech API
```bash
curl -X POST http://localhost:8080/api/v1/azure/speech/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"text":"안녕하세요","voice":"ko-KR-SunHiNeural"}'
```

**Kết quả mong đợi:**
```json
{
  "success": true,
  "text": "안녕하세요",
  "voice": "ko-KR-SunHiNeural",
  "audioData": "BASE64_ENCODED_MP3_DATA",
  "audioFormat": "audio/mpeg"
}
```

---

## 5️⃣ Frontend đã sẵn sàng

✅ **User Frontend** (`KoraStudy_FE`):
- `/flash-card/create` - Tạo flashcard với auto-translate và audio
- `/flash-card/edit/:id` - Chỉnh sửa với Azure features
- `/flash-card/practice/:id` - Luyện tập với phát âm

✅ **Admin Frontend** (`KoraStudy_FE_admin`):
- `/admin/flashcards/create` - Tạo flashcard hệ thống
- `/admin/flashcards/edit/:id` - Chỉnh sửa với Azure features

---

## 📊 Giới hạn Free Tier

| Service | Free Tier | Đủ cho |
|---------|-----------|--------|
| **Translator** | 2M ký tự/tháng | ~40,000 từ vựng/tháng |
| **Text-to-Speech** | 5M ký tự/tháng | ~100,000 phát âm/tháng |

---

## 🎯 Các tính năng đã tích hợp

### 1. Auto-Translate (Translator)
- Dịch tự động Korean → Vietnamese
- Detect ngôn ngữ tự động
- Button dịch ngay trên UI

### 2. Text-to-Speech
- 8 giọng đọc tiếng Hàn (4 nam, 4 nữ)
- Phát âm chuẩn native speaker
- Phát audio trực tiếp trên trình duyệt

### 3. UI/UX
- Loading state khi dịch/phát âm
- Toast notifications
- Icon trực quan (Languages, Volume2)
- Responsive design

---

## 🚀 Khởi động dự án

### Backend:
```bash
cd korastudy_be
mvn spring-boot:run
```

### Frontend User:
```bash
cd korastudy-fe
npm start
```

### Frontend Admin:
```bash
cd korastudy_fe_admin
npm start
```

---

## 🐛 Xử lý lỗi thường gặp

### Lỗi 401 Unauthorized
- Kiểm tra Azure Key đã đúng chưa
- Key có còn hiệu lực không (kiểm tra trong Azure Portal)

### Lỗi 403 Forbidden
- Kiểm tra Region có khớp không
- Free tier có thể bị limit rate (đợi 1 phút rồi thử lại)

### Lỗi CORS
- Backend đã config CORS cho Azure endpoints
- Kiểm tra `SecurityConfig.java` nếu cần

---

## 📚 Tài liệu tham khảo

- [Azure Translator Docs](https://learn.microsoft.com/azure/ai-services/translator/)
- [Azure Speech Service Docs](https://learn.microsoft.com/azure/ai-services/speech-service/)
- [Korean Voice List](https://learn.microsoft.com/azure/ai-services/speech-service/language-support?tabs=tts#text-to-speech)

---

✨ **Hoàn thành!** Flashcard của bạn giờ đã có AI dịch thuật và phát âm tiếng Hàn chuẩn!
