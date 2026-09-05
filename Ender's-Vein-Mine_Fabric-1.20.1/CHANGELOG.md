# Vein Mine 1.7.0 (Minecraft 1.21.11)

## Yenilikler ve Değişiklikler (Features & Changes)
* **Tam Sürüm Uyumluluğu:** Mod tamamen Minecraft 1.21.11 sürümüne ve en son Fabric API'sine güncellendi.
* **Gelişmiş Blok Parlatma Efekti (Block Highlighting):** Eski sürümlerde (1.20.x vb.) olduğu gibi hedef alınan bloklar artık hem daha kalın çerçevelerle hem de yarı saydam tam dolgu efektiyle parlıyor. Bu sayede kazılacak alan çok daha net anlaşılabiliyor.
* **Ayarlar Menüsü Desteği:** `Cloth Config` ve `ModMenu` kütüphanelerinin 1.21.11 destekli en yeni versiyonları entegre edildi. Ayarlar menüsü oyun içinden (varsayılan kısayol: **K** tuşu) veya ana menüdeki Mods sekmesinden açılabilir hale getirildi.
* **Derleme İsimlendirmeleri (Build Naming):** Çıktı alınan mod dosyaları artık `enders-vein-mine-[MOD_SÜRÜMÜ]-[MC_SÜRÜMÜ]-fabric.jar` (örneğin: `enders-vein-mine-1.7.0-1.21.11-fabric.jar`) formatında derleniyor, bu sayede mod listesinde karmaşa yaşanması önlendi.

## Hata Düzeltmeleri (Bug Fixes)
* **Türkçe Karakter (Locale) Hatası:** Oyun dili veya sistem dili Türkçe iken, şekil değiştirme mesajlarında ("Merdiven (Yukarı)" vb.) oluşan ve anahtarların ham (`config.veinmine.enum...`) haliyle gözükmesine sebep olan dil uyumsuzluğu (Büyük İ/ı dönüşüm) hatası düzeltildi.
* **Render Çökmesi (Crash):** Çizgi ve tam blok render'ı aynı anda oluşturulurken oluşan oyun motoru (Blaze3D) `Not building!` çökme sorunu çözüldü.
* **TitleScreen (ModMenu) Çökmesi:** Minecraft 1.21.11 menü revizyonlarına uygun olmayan eski ModMenu sürümlerinden kaynaklı `TitleScreen` IncompatibleClassChange / Mixin hataları, sürüm güncellemesiyle tamamen giderildi.
