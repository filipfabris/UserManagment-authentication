# UserManagment-authentication
* Demonstartion of usage symmetric cryptography and derivate key for user managment tool

## UserManagment.java
* Compile and start using:
```bash
mvn package

java -cp target/autentification-1.0-SNAPSHOT.jar hr.fer.srs.UserManagement
```
* You can not start program in IntelliJ or Eclipse console/prompt because of usage `Console console = System.console()`
* Scanner is not used in this instance because we want to hide password input from user in cosole for security reasons
* Password is not shown using `console.readPassword()`
* Because of this please start program in `Bash` or `Powershell`
* Command syntax:
```
add [username]          //Add user to managment tool
                        //Program will then ask you to enter password for user two times
delete [username]       //Delete username

passwd [username]       //Change password for user
                        //You will have to enter new password two times
forcepass [username]    //On next login user will need to change his password
```
* Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must have at least 8 characters

## Login.java
* Compile and start using:
```bash
mvn package

java -cp target/autentification-1.0-SNAPSHOT.jar hr.fer.srs.Login
```
* You can not start program in IntelliJ or Eclipse console/prompt because of usage `Console console = System.console()`
* Scanner is not used in this instance because we want to hide password input from user in cosole for security reasons
* Password is not shown using `console.readPassword()`
* Because of this please start program in `Bash` or `Powershell`
* Command syntax:
```
login [username]        //Program will then ask you to enter password which has been set by admin

//If admin set forcepass after you have logged in you will have to enter new password two times
//Password must be different from old one
```
* Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must have at least 8 characters

