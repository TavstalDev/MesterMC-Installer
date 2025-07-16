# MesterMC Telepítő

[![English Version](https://img.shields.io/badge/English%20Version-Language-blue?style=flat-square)](https://github.com/TavstalDev/MesterMC-Installer/blob/master/README.md)

![JavaFX](https://img.shields.io/badge/JavaFX-Cross--Platform%20UI-blue?logo=javafx)
![JDK 21+](https://img.shields.io/badge/JDK-21%2B%20Required-orange?logo=openjdk)
![Licenc](https://img.shields.io/github/license/TavstalDev/MesterMC-Installer)
![GitHub Problémák](https://img.shields.io/github/issues/TavstalDev/MesterMC-Installer)
[![Github All Releases](https://img.shields.io/github/downloads/TavstalDev/MesterMC-Installer/total.svg)]()

A **MesterMC Telepítő** egy többplatformos megoldás, amelyet a MesterMC indítóprogramjának telepítésének egyszerűsítésére terveztek különböző operációs rendszereken. Míg az eredeti MesterMC telepítő elsősorban Windowst támogatja, ez a telepítő kiterjeszti a kompatibilitást Linuxra és macOS-re is, biztosítva, hogy a szélesebb felhasználói bázis könnyedén beállíthassa játék kliensét.

## ✨ Funkciók

* **Többplatformos Kompatibilitás:** Telepítse a MesterMC-t zökkenőmentesen Windowsra, Linuxra és macOS-re.
* **Felhasználóbarát Grafikus Felület:** Egyértelmű és intuitív lépésről lépésre haladó varázsló vezet végig a telepítési folyamaton.
* **Testreszabható Telepítési Útvonal:** Válassza ki, hová szeretné telepíteni a MesterMC indítóprogramot.
* **Parancsikon Létrehozása:** Dönthet asztali és/vagy Start Menü parancsikonok létrehozása mellett.
* **Licencszerződés Megjelenítése:** Tekintse át és fogadja el a MesterMC licencet közvetlenül a telepítőben.
* **Telepítési Folyamat Nyomon Követése:** Valós időben figyelemmel kísérheti a telepítés előrehaladását.
* **Automatikus Indítás Opció:** Indítsa el a MesterMC-t azonnal a telepítés befejezése után.
* **Függőségkezelés:** Kezeli a szükséges rendszerinterakciókat a zökkenőmentes beállítás érdekében.

## 🖥️ Támogatott Operációs Rendszerek

Ez a telepítő a következő rendszereken való futtatásra készült:

* **Windows**
* **Linux**
* **macOS**

## 🚀 Kezdő lépések

### Előfeltételek

A MesterMC Telepítő futtatásához **JDK 21 vagy újabb** verzió szükséges a rendszerén.
* **Az Oracle JDK** erősen ajánlott.
* **A Red Hat JDK** nem kompatibilis ezzel a telepítővel.

### Telepítés és Használat

1.  **Töltse le a Telepítőt:**
    * **Windows:** Töltse le a `MesterMC-Installer-v1.0.0-win.zip` fájlt. Bontsa ki, és futtassa a `MesterMC-Installer.exe` végrehajtható fájlt.
    * **MacOS:** Töltse le a ``MesterMC-Installer-v1.0.0-macos.zip`` fájlt. Bontsa ki, és futtassa a `MesterMC-Installer.app` végrehajtható fájlt.
    * **Linux:**  Töltse le a ``MesterMC-Installer-v1.0.0-linux.tar.gz`` vagy a ``MesterMC-Installer-v1.0.0-linux.AppImage`` fájlt.
    * * **AppImage:**
    * * * **A. Terminálból:**
    ```bash
    chmod +x MesterMC-Installer-v1.0.0-linux.AppImage
    ./MesterMC-Installer-v1.0.0-linux.AppImage
    ```
    * * * **B. Fájlkezelőből:** Kattintson jobb gombbal a ``.AppImage`` fájlra, válassza a „Tulajdonságok” menüpontot, majd a „Jogosultságok” fülön jelölje be az „Engedélyezés futtatásra programként” lehetőséget. Ezután dupla kattintással indítsa el a fájlt.
    * * * **C. AppImageLauncher használatával:** Ha telepítve van az AppImageLauncher, egyszerűen dupla kattintással futtathatja az ``.AppImage`` fájlt, amely ezt kezeli helyetted.
    * * **.tar.gz:** Töltse le a ``.tar.gz`` fájlt. Csomagolja ki, majd futtassa a kibontott mappában a ``bin/MesterMC-Installer`` AppImage-t.

2.  **Kövesse a Képernyőn Megjelenő Utasításokat:**
    A telepítő a következő lépéseken keresztül vezeti Önt:
    * **Üdvözlő Képernyő:** Üdvözli Önt, és áttekintést nyújt.
    * **Licencszerződés:** Bemutatja a MesterMC licencet áttekintésre és elfogadásra.
    * **Telepítési Útvonal Kiválasztása:** Lehetővé teszi a MesterMC kívánt könyvtárának kiválasztását.
    * **Parancsikon Beállítások:** Konfigurálja az asztali és/vagy Start Menü parancsikonokat (ha az operációs rendszeréhez illeszkedik).
    * **Telepítés Áttekintése:** Összefoglalja a kiválasztott opciókat a folytatás előtt.
    * **Telepítési Folyamat:** Megjeleníti a folyamatban lévő telepítési folyamatot.
    * **Telepítés Befejeződött:** Megerősíti a sikeres telepítést, és felajánlja a MesterMC indítását.

## 🛠️ Használt technológiák

* **JavaFX:** A többplatformos grafikus felhasználói felület felépítéséhez.
* **Apache HttpClient:** HTTP kérések kezelésére (pl. `.jar` fájl méretének ellenőrzése).
* **FXML:** Deklaratív felhasználói felület tervezéséhez.
* **PowerShell:** Speciális rendszerinterakciókhoz használva, például parancsikonok létrehozásához Windows rendszeren.
* **AppImage:** Az alkalmazás Linuxon történő csomagolásához.

## 🤝 Hozzájárulás

Szívesen fogadjuk a MesterMC Telepítő projekthez való hozzájárulásokat!

> **Figyelem:** A dokumentáció illetve a kód csak angolul érhető el, de a hozzájárulásokat magyarul is szívesen fogadjuk!
> A hibajegyeket és a 'pull kéréseket' angolul kell benyújtani, de a hozzászólásokban magyarul is kommunikálhat.

Kérjük, olvassa el a [Hozzájárulási útmutatót](https://github.com/TavstalDev/MesterMC-Installer/blob/master/docs/building/getting-started.md) a részletes útmutatásért, hogyan járulhat hozzá a projekthez. Minden javaslatot és hibajavítást szívesen fogadunk!

## 📸 Képernyőképek

![Installer Step 0: Language](docs/images/lang_selector.png)
![Installer Step 1: Welcome Screen](docs/images/welcome.png)
![Installer Step 2: License Agreement](docs/images/license.png)
![Installer Step 3: Select Install Dir](docs/images/install_dir.png)
![Installer Step 4: Shortcuts](docs/images/shortcuts.png)
![Installer Step 5: Review](docs/images/review.png)
![Installer Step 6: Installing](docs/images/installing.png)
![Installer Step 7: Installed](docs/images/installed.png)

## 📜 Licenc

Ez a projekt az **MIT Licenc** alatt van licencelve. Részletekért lásd a `LICENSE` fájlt.

## ⚠️ Jogi nyilatkozat

**Ez a MesterMC telepítő egy rajongói projekt, és NINCS hivatalosan kapcsolatban az eredeti MesterMC csapattal vagy alkotóival, és ők nem is támogatják.**

## ❓ Támogatás és Kapcsolat

Ha bármilyen problémába ütközik, vagy kérdése van, forduljon hozzánk bizalommal:

* **Nyisson egy hibajegyet** a [GitHub Issues oldalon](https://github.com/tavstal/mmcinstaller/issues).
* **Közvetlen kapcsolat**
* * **Discord:** @Tavstal (Preferált, de nem kötelező)
* * **Twitter X:** [@Tavstal](https://x.com/Tavstal)

## ⛏️ MesterMC Hivatalos Linkek
* **Weboldal:** [MesterMC](https://mestermc.hu/)
* **Telepítő:** [MesterMC Telepítő](https://mestermc.eu/)
* **Discord:** [MesterMC Discord](https://discord.gg/mestermc)