# MesterMC Telepítő

[![English Version](https://img.shields.io/badge/English%20Version-Language-blue?style=flat-square)](https://github.com/TavstalDev/MesterMC-Installer/blob/master/README.md)

![JavaFX](https://img.shields.io/badge/JavaFX-Cross--Platform%20UI-blue?logo=javafx)
![JDK 21+](https://img.shields.io/badge/JDK-21%2B%20Required-orange?logo=openjdk)
![Licenc](https://img.shields.io/github/license/TavstalDev/MesterMC-Installer)
![GitHub Problémák](https://img.shields.io/github/issues/TavstalDev/MesterMC-Installer)

A **MesterMC Telepítő** egy többplatformos megoldás, amelyet a MesterMC indítóprogramjának telepítésének egyszerűsítésére terveztek különböző operációs rendszereken. Míg az eredeti MesterMC telepítő elsősorban Windowst támogatja, ez a telepítő kiterjeszti a kompatibilitást Linuxra és macOS-re is, biztosítva, hogy a szélesebb felhasználói bázis könnyedén beállíthassa játék kliensét.

## ✨ Funkciók

* **Többplatformos Kompatibilitás:** Telepítse a MesterMC-t zökkenőmentesen Windowsra, Linuxra és macOS-re.
* **Felhasználóbarát Grafikus Felület:** Egyértelmű és intuitív lépésről lépésre haladó varázsló vezet végig a telepítési folyamaton.
* **Testreszabható Telepítési Útvonal:** Válassza ki, hová szeretné telepíteni a MesterMC indítóprogramot.
* **Parancsikon Létrehozása:** Dönthet asztali és/vagy Start Menü parancsikonok létrehozása mellett (Windows).
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
    * **Windows:** Töltse le a `.zip` fájlt. Bontsa ki, és futtassa a `MesterMC_Installer.exe` végrehajtható fájlt.
    * **Linux:** Töltse le a `.zip` fájlt. Bontsa ki, és futtassa a mellékelt `.sh` szkriptet (pl. `bash install.sh`).
    * **macOS:** Töltse le a `.zip` fájlt. Bontsa ki, és futtassa a mellékelt `.zsh` szkriptet (pl. `zsh install.zsh`).

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

## 🤝 Hozzájárulás

Szívesen fogadjuk a MesterMC Telepítő projekthez való hozzájárulásokat!

### Fejlesztői környezet beállítása

1.  **Klónozza a Repositort:**
    ```bash
    git clone https://github.com/TavstalDev/MesterMC-Installer.git
    cd MesterMC-Installer
    ```
2.  **Nyissa meg az IntelliJ IDEA-ban:** Importálja a projektet az IntelliJ IDEA-ba. A Gradle build rendszer automatikusan letölti és konfigurálja a szükséges csomagokat.
3.  **Az Alkalmazás Tesztelése:**
    Futtathatja a telepítőt közvetlenül az IDE-ből a változtatások vagy új funkciók teszteléséhez.
    ```bash
    ./gradlew run
    ```
4.  **Az alkalmazás fordítása:**
    A telepítő fordításához használja a `jpackageImage` Gradle feladatot:
    ```bash
    ./gradlew jpackageImage
    ```
    Ez generálja a platformspecifikus telepítőcsomagokat.

## 📸 Képernyőképek

*(Még nincsenek képernyőképek. Ez a rész hamarosan frissül a telepítő vizuális példáival.)*

## 📜 Licenc

Ez a projekt az **MIT Licenc** alatt van licencelve. Részletekért lásd a `LICENSE` fájlt.

## ⚠️ Jogi nyilatkozat

**Ez a MesterMC telepítő egy rajongói projekt, és NINCS hivatalosan kapcsolatban az eredeti MesterMC csapattal vagy alkotóival, és ők nem is támogatják.**

## ❓ Támogatás és Kapcsolat

Ha bármilyen problémába ütközik, vagy kérdése van, forduljon hozzánk bizalommal:

* **Nyisson egy hibajegyet** a [GitHub Issues oldalon](https://github.com/tavstal/mmcinstaller/issues).
* **Közvetlenül vegye fel velem a kapcsolatot** (Kérjük, adja meg, hogyan szeretné felvenni velem a kapcsolatot, pl. GitHub profil, e-mail vagy egy adott platformon).