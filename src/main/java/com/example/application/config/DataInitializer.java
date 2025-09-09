package com.example.application.config;

import com.example.application.entity.MemeConfiguration;
import com.example.application.repository.MemeConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize default data on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MemeConfigurationRepository memeConfigurationRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeMemeConfigurations();
    }

    private void initializeMemeConfigurations() {
        // Check if meme configurations already exist
        if (memeConfigurationRepository.count() > 0) {
            return; // Already initialized
        }

        // Default meme images (placeholder URLs)
        String[] defaultImages = {
            "https://i.imgflip.com/1bij.jpg", // Grumpy Cat
            "https://i.imgflip.com/30b1gx.jpg", // Drake pointing
            "https://i.imgflip.com/1g8my4.jpg", // Distracted boyfriend
            "https://i.imgflip.com/26am.jpg", // Surprised Pikachu
            "https://i.imgflip.com/1otk96.jpg" // This is fine dog
        };

        for (int i = 0; i < defaultImages.length; i++) {
            MemeConfiguration imageConfig = new MemeConfiguration(MemeConfiguration.MemeType.IMAGE_URL, defaultImages[i]);
            imageConfig.setDisplayOrder(i + 1);
            memeConfigurationRepository.save(imageConfig);
        }

        // Default meme texts in Mongolian
        String[] defaultTexts = {
            "Өрөө төлөхгүй бол интернэт тасална! 😄",
            "Шимэгчлэхээ болиод, өрөө төлөөрэй! 🤣",
            "Өрийн зэрэглэл: Хувалз ➡️ Өндөр эрсдэлтэй 📈",
            "Төлбөр хийхгүй бол... та мэднэ дээ 😏",
            "Өрөө төлөх цаг болжээ! ⏰💰",
            "Санхүүгийн дисциплин = Амьдралын чанар 💪",
            "Өрөө төлөөд, амар амгалан амьдрая! 😌",
            "Хувалз байхаас өндөр эрсдэлтэй болох нь дээр үү? 🤔",
            "Төлбөрийн түүх: Сайн ➡️ Муу ➡️ Маш муу 📉",
            "Өрөө төлөөд, зүгээр амьдрая! 🏠💙"
        };

        for (int i = 0; i < defaultTexts.length; i++) {
            MemeConfiguration textConfig = new MemeConfiguration(MemeConfiguration.MemeType.TEXT, defaultTexts[i]);
            textConfig.setDisplayOrder(i + 1);
            memeConfigurationRepository.save(textConfig);
        }
    }
}
