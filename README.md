# 🏠 Shimegch - Өрийн мэдээллийн систем

Монгол хэл дээрх өрийн мэдээллийн удирдлагын систем. Excel файлаас өгөгдөл импорт хийж, PDF постер үүсгэх боломжтой.

## ✨ Онцлогууд

- 📊 **Excel импорт** - Өрийн мэдээллийг Excel файлаас татаж авах
- 📄 **PDF постер үүсгэх** - Өр төлөгчдөд зориулсан мэдэгдэл постер
- 🖼️ **Meme дэмжлэг** - Постер дээр зураг болон emoji харуулах
- 📱 **Responsive дизайн** - Mobile болон desktop дээр ажиллана
- 🔍 **Шүүлтүүр** - Байр, орц, давхар, өрийн зэрэглэлээр шүүх
- 🗄️ **Database** - H2 (development), PostgreSQL (production)

## 🚀 Технологи

- **Backend**: Java 21, Spring Boot 3.5.5
- **Frontend**: Vaadin 24.8.7
- **Database**: H2 (dev), PostgreSQL (prod)
- **PDF**: iText 7
- **Build**: Maven
- **Deploy**: Railway + Docker

## 🛠️ Локал дээр ажиллуулах

### Шаардлага
- Java 21+
- Maven 3.6+

### Эхлүүлэх
```bash
# Repository clone хийх
git clone https://github.com/yourusername/shimegch.git
cd shimegch

# Dependencies татах
mvn clean install

# Application эхлүүлэх
mvn spring-boot:run
```

Дараа нь http://localhost:8080 руу орно.

## 🌐 Production Deploy

### Railway дээр deploy хийх:
1. GitHub repository үүсгэх
2. Railway.app дээр бүртгүүлэх
3. GitHub repository холбох
4. PostgreSQL database нэмэх
5. Auto-deploy тохируулах

### Environment Variables:
- `DATABASE_URL` - PostgreSQL холболтын URL
- `PORT` - Server port (default: 8080)

## 📋 Хэрэглэх заавар

1. **Excel файл upload** - "Өгөгдөл оруулах" хэсэгт Excel файл upload хийнэ
2. **Өгөгдөл харах** - Үндсэн хуудсанд бүх айлуудын жагсаалт харагдана
3. **Шүүлтүүр** - "🔍 Шүүлтүүр" товчлуур дарж шүүлт хийнэ
4. **PDF постер** - Айл дээр дарж "📄 Постер үүсгэх" товчлуур дарна

## 🎨 Дэлгэцийн зураг

### Үндсэн хуудас
- Айлуудын жагсаалт
- Шүүлтүүр modal
- Responsive table

### PDF постер
- Айлын мэдээлэл
- Өрийн хэмжээ
- Meme зураг
- Монгол бичиг дэмжлэг

## 🤝 Хувь нэмэр оруулах

1. Repository fork хийх
2. Feature branch үүсгэх
3. Өөрчлөлт хийх
4. Pull request илгээх

## 📄 License

MIT License - дэлгэрэнгүйг LICENSE файлаас харна уу.

## 📞 Холбоо барих

Асуулт, санал байвал GitHub Issues дээр бичнэ үү.

---
Made with ❤️ for Mongolian communities
