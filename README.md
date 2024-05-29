### Usage
**1. Clone the Repository**
Clone the repository to your local machine:

```
git clone git@github.com:agudelo816/itext-poc.git
cd itext-poc
```

**2. Build the Project**
Build the project using Maven:

```
mvn clean install
```

**3. Prepare the Input PDF**
Ensure you have an existing PDF file named input.pdf in the project directory. This is the file that will be signed and password protected.

**4. Run the Application**
Run the application with the following command:
```
mvn exec:java -Dexec.mainClass="Main"
```

**5. Check the Output**
After running the application, you will find the following files in your project directory:

```
signed_input.pdf: The signed PDF.
protected_input.pdf: The signed and password-protected PDF.
```

### Configuration
The application is configured to use the following default values:

Keystore File: keystore.p12
Keystore Password: password
Key Alias: alias
Input PDF: input.pdf
Signed PDF: signed_input.pdf
Password Protected PDF: protected_input.pdf
Owner Password: ownerPassword
User Password: userPassword
You can modify these values directly in the Main.java file if needed.
