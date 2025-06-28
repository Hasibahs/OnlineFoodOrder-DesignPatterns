# Online Food Ordering App

This is a JavaFX desktop application for food ordering.

## 📦 Out-of-the-Box Running Instructions

### Prerequisites
- **Java 17 or newer** must be installed.
  - To check, open Command Prompt and run:  
    ```
    java -version
    ```

### Included in this Project
- `foodorderapp-1.0-SNAPSHOT.jar` (the application)
- `run.bat` (Windows startup script)
- `lib/javafx-sdk-21/lib/` (JavaFX libraries)
- Example SQLite databases (if needed)

### How to Run

1. **Download and extract the ZIP file** so you see this structure:
    ```
    - Navigate to `OnlineFoodOrderingApp/OnlineFoodOrderingApp/target/`
    ```

2. **Double-click `run.bat`.**

    - The app should launch in a few seconds.
    - If you see a security warning, click "More info" and "Run anyway".

3. **If you get an error:**
    - Make sure you extracted all folders, not just the `.jar` and `.bat` files.
    - Make sure Java is installed and you are running `run.bat` from the **project root folder**.

4. **For advanced users (optional):**  
   You can also run the app from command line:
    ```sh
    java --module-path "lib/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml -jar foodorderapp-1.0-SNAPSHOT.jar
    ```

---

## Notes

- No installation required. No additional setup required. All libraries are included!
- This setup works **out of the box** on Windows.  
- If you use macOS or Linux, adjust the JavaFX `--module-path` accordingly and use `sh run.sh` instead.

---
5. **Admin Login**

- Username: admin

- Password: 12345

You can use these credentials to log in as an administrator and manage users, menu items, and toppings.

## Troubleshooting

- **Error:** `Module javafx.controls not found`
    - Solution: Check that the folder `lib/javafx-sdk-21/lib` exists and contains all `javafx-*.jar` files.
- **Error:** `java not recognized`
    - Solution: [Download and install Java 17+](https://adoptium.net/).

---

## Credits

- JavaFX 21 SDK from [gluonhq.com](https://gluonhq.com/products/javafx/)

---

Enjoy your food ordering experience!
