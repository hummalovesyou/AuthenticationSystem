import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

// Моделирование политики разграничения доступа.
public class AuthenticationSystem implements Serializable {
    private static final String FLAG_FILE_NAME = "flag.txt"; // Флаг для проверки, запускается ли программа впервые.
    private int loginAttempts; // Попыток входа.
    private List<UserAccount> userAccounts = new ArrayList<>(); // Список для хранения пользователей
    private List<Roles> roles = new ArrayList<>(); // Список для хранения ролей
    private List<MyFile> files = new ArrayList<>(); // Список для хранения файлов

    private static final String FILENAMEUser = "users.txt"; // Имя файла для хранения учетных записей
    private static final String FILENAMERoles = "roles.txt"; // Имя файла для хранения учетных записей
    private static final String FILENAMEFiles = "files.txt"; // Имя файла для хранения учетных записей
    private Pattern passwordPattern = Pattern.compile("^[A-Z?#]+$");

    public void resetLoginAttempts() {
        loginAttempts = 0;
    }

    public void incrementLoginAttempts() {
        loginAttempts++;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public AuthenticationSystem() {
        loadFromFile(); // Загрузить учетные записи из файла при создании объекта
    }

    // Проверка наличия флага
    private boolean checkFlag() {
        File flagFile = new File(FLAG_FILE_NAME);
        return flagFile.exists();
    }

    // Установка флага
    private void setFlag() {
        try {
            File flagFile = new File(FLAG_FILE_NAME);
            flagFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Выполнение фрагмента кода
    private void executeCodeFragment() {
        addRoleStart("Проектный руководитель", 1);
        addRoleStart("Технический лидер", 1);
        addRoleStart("Разработчик", 10);
        addRoleStart("Тестировщик", 2);
        addRoleStart("Аналитик", 1);

        // Создание пользователей
        addAccountStart("John", "A#A#A#A#A#", 1);
        addAccountStart("Bob", "A#A#A#A#A#", 2);
        addAccountStart("Eva", "A#A#A#A#A#", 3);
        addAccountStart("Max", "A#A#A#A#A#", 4);
        addAccountStart("Sasha", "A#A#A#A#A#", 5);

        // Создание файлов
        // Техническое задание.
        //    Проектный руководитель: READ
        //    Технический лидер: READ, WRITE
        //    Аналитик: READ
        //    Разработчик: Нет доступа
        //    Тестировщик: Нет доступа
        addFileStart("techno.txt");
        createFile("techno.txt");
        setPermissions("techno.txt", Roles.getRoleByName("Проектный руководитель"), Permission.READ);
        setPermissions("techno.txt", Roles.getRoleByName("Технический лидер"), Permission.READ, Permission.WRITE);
        setPermissions("techno.txt", Roles.getRoleByName("Аналитик"), Permission.READ);

        // Исходный код проекта.
        //    Технический лидер: READ, WRITE
        //    Разработчик: READ, WRITE
        //    Проектный руководитель: READ
        //    Тестировщик: Нет доступа
        //    Аналитик: Нет доступа
        addFileStart("code.txt");
        createFile("code.txt");
        setPermissions("code.txt", Roles.getRoleByName("Технический лидер"), Permission.READ, Permission.WRITE);
        setPermissions("code.txt", Roles.getRoleByName("Разработчик"), Permission.READ, Permission.WRITE);
        setPermissions("code.txt", Roles.getRoleByName("Проектный руководитель"), Permission.READ);

        // Тестовая документация.
        //    Технический лидер: READ, WRITE
        //    Тестировщик: READ
        //    Проектный руководитель: Нет доступа
        //    Разработчик: Нет доступа
        //    Аналитик: Нет доступа
        addFileStart("test.txt");
        createFile("test.txt");
        setPermissions("test.txt", Roles.getRoleByName("Технический лидер"), Permission.READ, Permission.WRITE);
        setPermissions("test.txt", Roles.getRoleByName("Тестировщик"), Permission.READ);
    }

    public void addAccount(String username, String password, int chooseRole) {
        if (isValidPassword(password)) {
            if (!identifyUser(username)) {
                Roles role = roles.get(chooseRole);
                if (!role.isCorrectToAdd()) {
                    System.out.println("Для данной роли превышен лимит добавления пользователей. Пользователь будет создан без роли");
                    UserAccount newUser = new UserAccount(username, encryptPassword(password));
                    userAccounts.add(newUser);
                    newUser.setLengthPassword(password.length());
                } else {
                    role.upCount();
                    UserAccount newUser = new UserAccount(username, encryptPassword(password), role);
                    userAccounts.add(newUser);
                    newUser.setLengthPassword(password.length());
                    System.out.println("Пользователь " + username + " c ролью " + roles.get(chooseRole) + " создан!");
                }
                saveToFile(); // Сохранить учетные записи в файл после добавления
            } else {
                System.out.println("Пользователь с данным именем уже существует!");
            }
        } else {
            System.out.println("Пароль не подходит под указанные требования!");
        }
    }

    public void addAccountStart(String username, String password, int chooseRole) {
        if (isValidPassword(password)) {
            if (!identifyUser(username)) {
                Roles role = roles.get(chooseRole - 1);
                if (!role.isCorrectToAdd()) {
                    role = roles.get(5); // Присваивание пустой роли в случае, если выбранная роль заполнена.
                } else {
                    role.upCount();
                }
                UserAccount newUser = new UserAccount(username, encryptPassword(password), role);
                userAccounts.add(newUser);
                newUser.setLengthPassword(password.length());
                saveToFile(); // Сохранить учетные записи в файл после добавления
            }
        }
    }

    // Добавление роли
    public void addRole(String role, int quantity) {
        if (!identifyRole(role)) {
            Roles newRole = new Roles(role, quantity);
            roles.add(newRole);
            saveToFile();
            System.out.println("Роль создана!");
        } else System.out.println("Роль с таким именем уже существует!");
    }

    public void addRoleStart(String role, int quantity) {
        if (!identifyRole(role)) {
            Roles newRole = new Roles(role, quantity);
            roles.add(newRole);
            saveToFile();
        }
    }

    public void addFile(String file) {
        if (!identifyFile(file)) {
            MyFile newFile = new MyFile(file);
            files.add(newFile);
            saveToFile(); // Сохраняем изменения
            System.out.println("Файл " + file + " создан!");
        } else System.out.println("Файл с таким именем уже существует!");
    }

    public void addFileStart(String file) {
        if (!identifyFile(file)) {
            MyFile newFile = new MyFile(file);
            files.add(newFile);
            saveToFile(); // Сохраняем изменения
        }
    }

    // Возвращает юзера
    public UserAccount findUser(String username) {
        for (UserAccount userAccount : userAccounts) {
            if (username.equals(userAccount.getUsername()))
                return userAccount;
        }
        return null;
    }

    // Идентификация пользователя.
    public boolean identifyUser(String username) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    // Идентификация роли.
    public boolean identifyRole(String roleName) {
        for (Roles role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    // Идентификация роли администратора и возврат ее.
    public Roles identifyRoleAdmin() {
        for (Roles role : roles) {
            if (role.getName().equals("Проектный руководитель")) {
                return role;
            }
        }
        return null;
    }

    // Идентификация файла.
    public boolean identifyFile(String fileName) {
        for (MyFile file : files) {
            if (file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    // Аутентификация пользователя.
    public boolean authenticateUser(String username, String password) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username) && isEqualPasswords(username, password)) {
                return true;
            }
        }
        return false;
    }

    // Сравнение паролей (+расшифровка)
    public boolean isEqualPasswords(String username, String password) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                if (decryptPassword(user.getPassword(), user.getLengthPassword()).equals(password)) return true;
            }
        }
        return false;
    }

    // Метод для установки разрешений для роли к файлу
    public void setPermissions(String fileName, Roles role, Permission... permissions) {
        if (identifyFile(fileName)) {
            for (MyFile file : files) {
                if (file.getName().equals(fileName)) {
                    file.addPermissions(role, permissions);
                    saveToFile();
                }
            }

        } else {
            throw new IllegalArgumentException("Файл не существует: " + fileName);
        }
    }

    public void addRoleToUser(String username, Roles role) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                user.addRole(role);
                role.upCount();
                saveToFile();
            }
        }
    }

    public void removeRoleToUser(String username, Roles role) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                user.removeRole(role);
                role.reduceCount();
                saveToFile();
            }
        }
    }

    public List<Roles> allRolesOfUser(String username) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                return user.getRoles();
            }
        }
        return null;
    }

    public void allRolesOfUserssout(String username) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                for (Roles role : user.getRoles()) {
                    System.out.println(role.getName());
                }
            }
        }
    }

    // Выбор роли для пользователя
    public void chooseRole(UserAccount user, int num) {
        user.chooseRole(num);
    }

    // Метод для сохранения данных в файл
    public void saveToFile() {
        try (FileOutputStream fos = new FileOutputStream(FILENAMEUser);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userAccounts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream(FILENAMERoles);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(roles);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fos = new FileOutputStream(FILENAMEFiles);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для загрузки файла
    private void loadFromFile() {
        try (FileInputStream fisUser = new FileInputStream(FILENAMEUser);
             ObjectInputStream oisUser = new ObjectInputStream(fisUser)) {
            if (fisUser.available() > 0) {
                userAccounts = (List<UserAccount>) oisUser.readObject();
            }
        } catch (EOFException e) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (FileInputStream fisRoles = new FileInputStream(FILENAMERoles);
             ObjectInputStream oisRoles = new ObjectInputStream(fisRoles)) {
            if (fisRoles.available() > 0) {
                roles = (List<Roles>) oisRoles.readObject();
            }
        } catch (EOFException e) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (FileInputStream fisFiles = new FileInputStream(FILENAMEFiles);
             ObjectInputStream oisFiles = new ObjectInputStream(fisFiles)) {
            if (fisFiles.available() > 0) {
                files = (List<MyFile>) oisFiles.readObject();
            }
        } catch (EOFException e) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    // Изменение пароля пользователя
    public void editAccount(String username, String newPassword) {
        if (isValidPassword(newPassword)) {
            for (UserAccount user : userAccounts) {
                if (user.getUsername().equals(username)) {
                    user.setPassword(encryptPassword(newPassword));
                    System.out.println("Пароль пользователя изменен!");
                    saveToFile(); // Сохранить обновленные данные в файл
                    return;
                }
            }
            System.out.println("Пользователь не найден.");
        } else {
            System.out.println("Новый пароль не подходит под условия варианта!");
        }
    }

    // Проверка валидности пароля
    public boolean isValidPassword(String password) {
        return passwordPattern.matcher(password).matches() && password.length() >= 8;
    }

    // Удаление пользователя
    public void removeUser(UserAccount user) {
        if (user != null) {
            System.out.println("Пользователь " + user.getUsername() + " удален!");
            userAccounts.remove(user);
            saveToFile(); // Сохранить обновленный список пользователей в файл
        } else {
            System.out.println("Роль не найдена!");
        }
    }

    // Удаление роли
    public void removeRole(Roles role) {
        if (role.getName().equals("Проектный руководитель")) {
            System.out.println("Невозможно удалить роль Проектный руководитель!");
        } else if (role != null) {
            roles.remove(role);
            for (UserAccount user : userAccounts) {
                user.removeRole(role);
            }
            System.out.println("Роль " + role.getName() + " удалена!");
            saveToFile(); // Сохранить обновленный список пользователей в файл
        } else {
            System.out.println("Роль не найдена!");
        }
    }

    // Удаление файла
    public void removeFile(MyFile file) {
        if (file != null) {
            System.out.println("Файл " + file.getName() + " удален!");
            files.remove(file);
            saveToFile(); // Сохранить обновленный список пользователей в файл
        } else {
            System.out.println("Файл не найден!");
        }
    }

    // Просмотр всех пользователей
    public void viewAllUsers() {
        System.out.println("Список всех пользователей:");
        for (UserAccount user : userAccounts) {
            System.out.println(user.toString());
        }
    }

    // Просмотр всех имен пользователей
    public void viewAllUsersNames() {
        System.out.println("Список всех пользователей:");
        int i = 1;
        for (UserAccount user : userAccounts) {
            System.out.println(i + ". " + user.toStringTwo());
            i++;
        }
        System.out.println();
    }

    // Просмотр всех ролей
    public void viewAllRoles() {
        System.out.println("Список всех ролей:\n");
        int i = 1;
        for (Roles role : roles) {
            System.out.println(i + ". " + role.toString());
            i++;
        }
        System.out.println();
    }

    // Просмотр всех ролей пользователя
    public void viewAllUsersRoles(String username) {
        for (UserAccount user : userAccounts) {
            if (user.getUsername().equals(username)) {
                System.out.println("Список всех ролей пользователя:\n");
                int i = 1;
                for (Roles role : user.getRoles()) {
                    System.out.println(i + ". " + role.toString());
                    i++;
                }
                return;
            }
        }
    }


    // Просмотр вместимости всех ролей.
    public void viewingQuantityOfRoles() {
        System.out.println("Заполненность ролей: \n");
        for (Roles role : roles){
            System.out.println(role.getName() + ": " + role.getCount() + "/" + role.getQuantity() + ".");
        }
        System.out.println("\n");
    }

    // Просмотр всех файлов.
    public void viewAllFiles() {
        System.out.println("Список всех файлов:");
        int i = 1;
        for (MyFile file : files) {
            System.out.println(i + ". " + file.toString());
            i++;
        }
    }

    // Метод для просмотра всех файлов с разрешениями чтения.
    public void viewAllFilesWithPermissionRead(Roles role) {
        System.out.println("Файлы с разрешением чтения для роли " + role.getName() + ":");
        for (MyFile file : files) {
            if (file.canRead(role)) {
                System.out.println(file.getName());
            }
        }
    }

    // Метод для просмотра всех файлов с разрешениями записи.
    public void viewAllFilesWithPermissionWrite(Roles role) {
        System.out.println("Файлы с разрешением записи для роли " + role.getName() + ":");
        for (MyFile file : files) {
            if (file.canWrite(role)) {
                System.out.println(file.getName());
            }
        }
    }

    // Метод для проверки, может ли пользователь читать файл.
    public boolean canUserReadFile(UserAccount user, MyFile file) {
        Roles userRole = user.getRole();
        return userRole != null && (file.canRead(userRole));
    }

    // Метод для чтения содержимого файла.
    public void readFile(UserAccount user, MyFile file) {
        if (canUserReadFile(user, file)) {
            System.out.println("Чтение файла " + file.getName());
            try (Scanner scanner = new Scanner(new File(file.getName()))) {
                while (scanner.hasNextLine()) {
                    System.out.println(scanner.nextLine());
                }
            } catch (FileNotFoundException e) {
                System.out.println("Файл не найден: " + e.getMessage());
            }
        } else {
            System.out.println("У вас нет прав для чтения файла " + file.getName());
        }
    }

    // Метод для записи в файл.
    public void writeFile(UserAccount user, MyFile file) {
        if (canUserReadFile(user, file) && file.canWrite(user.getRole())) {
            System.out.println("Запись в файл " + file.getName());
            try (PrintWriter writer = new PrintWriter(new FileWriter(file.getName(), true))) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Введите текст для записи в файл:");
                String input = scanner.nextLine();
                writer.println(input);
                System.out.println("Запись в файл успешно выполнена.");
            } catch (IOException e) {
                System.out.println("Ошибка при записи в файл: " + e.getMessage());
            }
        } else {
            System.out.println("У вас нет прав для записи в файл " + file.getName());
        }
    }

    // Шифрование пароля (По варианту 6)
    public String encryptPassword(String password) {

        // Матрица для шифрования пароля
        Random rand = new Random();
        String str1 = "";
        int count = 0;
        int max = password.length();
        int z = -1;
        char[] alfabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        int index = password.length() / 5;
        int rem = password.length() % 5;
        if (rem != 0) {
            count = index + 1;
        } else {
            count = index;
        }

        for (
                int k = 0;
                k < count; k++) {
            char[][] matr = new char[3][3];

            for (int i = 0; i < 3; i++) {
                for (int j1 = 0; j1 < 3; j1++) {
                    index = rand.nextInt(25);
                    matr[i][j1] = alfabet[index];
                }
            }

            int j = 0;

            for (int i = 2; i > -1; i--) { // диагональ (вниз-вправо)
                if ((i + j == 2) && (z < max - 1)) {
                    z += 1;
                    matr[i][j] = password.charAt(z);
                    j++;
                }
            }


            for (j = 1; j < 3; j++) // вертикально (вверх)
            {
                z += 1;
                if (z < max) {
                    matr[j][2] = password.charAt(z);
                }
            }

            for (int i = 1; i > 0; i--) // горизонтально (влево)
            {
                z += 1;
                if (z < max) {
                    matr[2][i] = password.charAt(z);
                }
            }

            for (int i = 0; i < 3; i++) {
                for (j = 0; j < 3; j++) {
                    str1 += matr[i][j];
                }
            }
        }
        return str1;
    }



    // Расшифровка пароля.
    public static String decryptPassword(String encryptedPassword, int length) {
        int matrixSize = 3;
        int totalSize = matrixSize * matrixSize;

        StringBuilder decryptedPassword = new StringBuilder();

        for (int i = 0; i < encryptedPassword.length(); i += totalSize) {
            String matrixStr = encryptedPassword.substring(i, i + totalSize);
            char[][] matrix = new char[matrixSize][matrixSize];

            int strIndex = 0;
            for (int row = 0; row < matrixSize; row++) {
                for (int col = 0; col < matrixSize; col++) {
                    matrix[row][col] = matrixStr.charAt(strIndex);
                    strIndex++;
                }
            }

            int j = 0;
            for (int i2 = matrixSize - 1; i2 >= 0; i2--) {
                if ((i2 + j == 2)) {
                    decryptedPassword.append(matrix[i2][j]);
                    j++;
                }
            }

            for (int j2 = 1; j2 < matrixSize; j2++) {
                decryptedPassword.append(matrix[j2][matrixSize - 1]);
            }

            for (int i2 = 1; i2 > 0; i2--) {
                decryptedPassword.append(matrix[matrixSize - 1][i2]);
            }
        }

        if (decryptedPassword.length() > length) {
            decryptedPassword.setLength(length); // Обрезаем до заданной длины
        }

        return decryptedPassword.toString();
    }

    public String getPermissionsForRoleAndFile(String fileName, Roles role) {
        if (identifyFile(fileName)) {
            for (MyFile file : files) {
                if (file.getName().equals(fileName)) {
                    if (file.canRead(role) && file.canWrite(role)) {
                        return "rw";
                    } else if (file.canRead(role)) {
                        return "r";
                    } else if (file.canWrite(role)){
                        return "w";
                    } else return " ";
                }
            }
        } else {
            throw new IllegalArgumentException("Файл не существует: " + fileName);
        }
        return "";
    }

    // Метод для удаления разрешения в файле
    public void removePermission(String fileName, Roles role, Permission permission) {
        if (identifyFile(fileName)) {
            for (MyFile file : files) {
                if (file.getName().equals(fileName)) {
                    file.removePermission(role, permission);
                    saveToFile();
                } else {
                    throw new IllegalArgumentException("Выбранного разрешения для удаления не существует: " + fileName);
                }
            }
        }
    }

    // Метод для вывода таблицы с правами доступа
    public void printAccessTable() {
        // Вывод верхних двух строк
        System.out.print("\t\t\t\t\t|");
        for (Roles role : roles) {
            System.out.print(String.format("%-22s|", role.getName()));
        }
        System.out.println();

        System.out.print("\t\t\t\t\t|");
        for (Roles role : roles) {
            System.out.print("----------------------|");
        }
        System.out.println();

        // Вывод таблицы с правами доступа
        for (MyFile file : files) {
            String fileName = file.getName();
            System.out.print(fileName + "\t\t\t|");

            for (Roles role : roles) {
                if (file.canRead(role) && file.canWrite(role)) {
                    System.out.print(String.format("%-22s|", "rw"));
                } else if (file.canRead(role)) {
                    System.out.print(String.format("%-22s|", "r"));
                } else {
                    System.out.print(String.format("%-22s|", "")); // Пустая ячейка, если нет прав доступа
                }
            }

            System.out.println();
        }
    }

    // Создание файла
    public void createFile(String fileName) {
        try {
            File file = new File(System.getProperty("user.dir") + File.separator + fileName);
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("Произошла ошибка при создании файла: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        File userFile = new File(FILENAMEUser);
        File rolesFile = new File(FILENAMERoles);
        File filesFile = new File(FILENAMEFiles);

        boolean isAdmin = false;

        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!rolesFile.exists()) {
            try {
                rolesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!filesFile.exists()) {
            try {
                filesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        AuthenticationSystem authSystem = new AuthenticationSystem();

        // Проверка наличия флага
        if (!authSystem.checkFlag()) {
            // Выполнение фрагмента кода
            authSystem.executeCodeFragment();

            // Установка флага
            authSystem.setFlag();
        }

        Scanner sc = new Scanner(System.in);

        int number;
        if (!isAdmin) {
            try {
                do {
                    System.out.print("""
                            __________________________________________________________________________________________________________________
                            1) Авторизация;
                            2) Закрытие программы.
                            __________________________________________________________________________________________________________________
                            Выберите действие (1-2):\s""");


                    number = sc.nextInt();

                    switch (number) {

                        case 1:
                            try {
                                Scanner sc0 = new Scanner(System.in);
                                System.out.print("Логин: ");
                                String username1 = sc0.nextLine();
                                if (!authSystem.identifyUser(username1)) {
                                    System.out.println("Данного пользователя не существует. Попробуйте еще раз.");
                                    authSystem.incrementLoginAttempts();
                                    System.out.println("У вас осталось попыток: " + (3 - authSystem.getLoginAttempts()));
                                    if (authSystem.getLoginAttempts() >= 3) {
                                        System.out.println("Превышено количество попыток. Подождите некоторое время перед следующей попыткой.");
                                        Thread.sleep(5000);
                                        authSystem.resetLoginAttempts();
                                    }
                                    continue;
                                }
                                System.out.print("Пароль: ");
                                String password1 = sc0.nextLine();


                                if (authSystem.authenticateUser(username1, password1)) {
                                    System.out.println("Вы успешно вошли в систему!\n");

                                    UserAccount user = authSystem.findUser(username1);

                                    System.out.println("Выберите роль для входа:\n");
                                    List<Roles> userRoles = user.getRoles();

                                    int i = 1;
                                    for (Roles role : userRoles) {
                                        System.out.println(i + " - " + role.getName());
                                        i++;
                                    }
                                    if (userRoles.isEmpty()) {
                                        System.out.println("У Вас нет доступных ролей. Выход из системы.");
                                        break;
                                    }
                                    int roleChoice;
                                    do {
                                        System.out.print("\nВведите номер действия: ");
                                        roleChoice = sc.nextInt();

                                    } while (roleChoice < 1 || roleChoice > userRoles.size() );

                                    // Выбранная роль для входа
                                    Roles selectedRole = userRoles.get(roleChoice - 1);
                                    authSystem.chooseRole(user, roleChoice - 1);
                                    System.out.println("Вы вошли в систему под ролью: " + selectedRole.getName());


                                    isAdmin = user.getRole().isAdmin();
                                    authSystem.resetLoginAttempts();
                                    int num;
                                    if (isAdmin) {
                                        do {
                                            System.out.print("""
                                                        __________________________________________________________________________________________________________________
                                                        1) Создание пользователя;
                                                        2) Создание новой роли;
                                                        3) Создание нового файла;
                                                        4) Идентификацию;
                                                        5) Аутентификацию;
                                                        6) Изменение данных пользователя;
                                                        7) Удаление пользователя;
                                                        8) Удаление роли;
                                                        9) Удаление файла;
                                                        10) Редактирование прав доступа;
                                                        11) Список всех пользователей;
                                                        12) Список всех ролей;
                                                        13) Список всех файлов;
                                                        14) Таблица разрешений;
                                                        15) Список количественных ограничений для каждой роли;
                                                        16) Чтение файла;
                                                        17) Запись файла;
                                                        18) Выход из системы;
                                                        19) Завершение сеанса.
                                                        __________________________________________________________________________________________________________________

                                                        Выберите действие (1-19):\s""");
                                            num = sc.nextInt();
                                            switch (num) {
                                                // Создание пользователя.
                                                case 1:
                                                    try {
                                                        Scanner sc1 = new Scanner(System.in);
                                                        System.out.println("Придумайте логин пользователя: ");
                                                        String username = sc1.nextLine();
                                                        System.out.println("Придумайте пароль (Пароль может содержать только прописные латинские буквы, разделённые символом # и длиной 8+ символов): ");
                                                        String pass = sc1.nextLine();
                                                        if (!authSystem.isValidPassword(pass)) {
                                                            System.out.println("Пароль не подходит под условия варианта! Пользователь не будет создан!");
                                                            break;
                                                        }
                                                        authSystem.viewAllRoles();
                                                        System.out.println("Выберите роль для пользователя: ");
                                                        int chooseRole = sc1.nextInt();
                                                        authSystem.addAccount(username, pass, chooseRole-1);
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Создать новую роль.
                                                case 2:
                                                    try {
                                                        Scanner sc2 = new Scanner(System.in);
                                                        System.out.println("Придумайте название новой роли: ");
                                                        String role = sc2.nextLine();
                                                        System.out.println("Придумайте ограничение на количество пользователей для данной роли: ");
                                                        int quantity = sc2.nextInt();
                                                        authSystem.addRole(role, quantity);
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Создать новый файл.
                                                case 3:
                                                    try {
                                                        Scanner sc3 = new Scanner(System.in);
                                                        System.out.println("Придумайте название новому файлу: ");
                                                        String newFileName = sc3.nextLine();
                                                        authSystem.addFile(newFileName);
                                                        authSystem.createFile(newFileName);
                                                        authSystem.setPermissions(newFileName, authSystem.identifyRoleAdmin(), Permission.READ, Permission.WRITE);
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Идентификация.
                                                case 4:
                                                    try {
                                                        Scanner sc4 = new Scanner(System.in);
                                                        System.out.println("Введите имя пользователя: ");
                                                        String username = sc4.nextLine();
                                                        if (authSystem.identifyUser(username)) {
                                                            System.out.println("Пользователь идентифицирован!");
                                                        } else
                                                            System.out.println("Пользователь не найден.");

                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Аутентификация.
                                                case 5:
                                                    try {
                                                        Scanner sc5 = new Scanner(System.in);
                                                        System.out.println("Введите логин пользователя: ");
                                                        String username = sc5.nextLine();
                                                        System.out.println("Введите пароль пользователя: ");
                                                        String password = sc5.nextLine();
                                                        if (authSystem.identifyUser(username)) {
                                                            if (authSystem.authenticateUser(username, password)) {
                                                                System.out.println("Пользователь аутентифицирован.");
                                                            } else {
                                                                System.out.println("Ошибка аутентификации.");
                                                            }
                                                        } else {
                                                            System.out.println("Пользователь не найден.");
                                                        }

                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Изменение данных пользователя
                                                case 6:
                                                    try {
                                                        Scanner sc6 = new Scanner(System.in);
                                                        System.out.println("Выберите пользователя, для которого будут происходить изменения: \n");
                                                        authSystem.viewAllUsersNames();
                                                        int numChooseUser  = sc6.nextInt();
                                                        String username = authSystem.userAccounts.get(numChooseUser-1).getUsername();

                                                        System.out.println("\nДоступные действия: ");
                                                        System.out.println("1. Поменять пароль;\n" +
                                                                "2. Добавить роль;\n" +
                                                                "3. Удалить роль;\n\n" +
                                                                "Ваш выбор: ");
                                                        int number6 = sc6.nextInt();
                                                        switch (number6) {
                                                            case 1:
                                                                Scanner sc61 = new Scanner(System.in);
                                                                System.out.println("Введите новый пароль (Пароль может содержать только прописные латинские буквы, разделённые символом # и длиной 8+ символов): ");
                                                                String newPass = sc61.nextLine();
                                                                authSystem.editAccount(username, newPass);
                                                                break;

                                                            case 2:
                                                                Scanner sc62 = new Scanner(System.in);
                                                                System.out.println("Выберите роль, которую нужно добавить пользователю: ");
                                                                authSystem.viewAllRoles();
                                                                System.out.println("\nВаш выбор: ");
                                                                int numChooseRole = sc62.nextInt();
                                                                Roles newAddRole = authSystem.roles.get(numChooseRole - 1);
                                                                try {
                                                                    if (newAddRole.isCorrectToAdd()) {
                                                                        authSystem.addRoleToUser(username, newAddRole);
                                                                        System.out.println("Роль " + authSystem.roles.get(numChooseRole - 1).getName() + " успешно добавлена пользователю " + username + "!");
                                                                    } else System.out.println("Для данной роли превышен лимит добавления пользователей. Роль не будет добавлена.");
                                                                    } catch (Exception e) {
                                                                    System.out.println("Данной роли " + authSystem.roles.get(numChooseRole - 1).getName() + " не существует!");
                                                                }
                                                                break;

                                                            case 3:
                                                                Scanner sc63 = new Scanner(System.in);
                                                                System.out.println("Выберите роль, которую нужно удалить пользователю: ");
                                                                authSystem.viewAllUsersRoles(username);
                                                                System.out.println("\nВаш выбор: ");
                                                                int chooseRole = sc63.nextInt();
                                                                Roles newDeleteRole = authSystem.allRolesOfUser(username).get(chooseRole-1);
                                                                if (newDeleteRole.getName().equals("Проектный руководитель")) {
                                                                    System.out.println("У вас нет прав, чтобы удалить роль " + newDeleteRole.getName() + " у пользователя " + username + "!");
                                                                    break;
                                                                }
                                                                try {
                                                                    authSystem.removeRoleToUser(username, newDeleteRole);
                                                                    System.out.println("Роль " + newDeleteRole.getName() + " удалена!");
                                                                } catch (Exception e) {
                                                                    System.out.println("Роли " + newDeleteRole.getName() + " не существует!");
                                                                }
                                                                break;
                                                        }
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Удаление пользователя
                                                case 7:
                                                    try {
                                                        authSystem.viewAllUsersNames();
                                                        Scanner sc7 = new Scanner(System.in);
                                                        System.out.println("\nВыберите логин пользователя, которого нужно удалить: ");
                                                        int chooseUsername = sc7.nextInt();
                                                        authSystem.removeUser(authSystem.userAccounts.get(chooseUsername-1));
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Удаление роли.
                                                case 8:
                                                    try {
                                                        authSystem.viewAllRoles();
                                                        Scanner sc8 = new Scanner(System.in);
                                                        System.out.println("\nВыберите название роли, которую нужно удалить: ");
                                                        int chooseRolename = sc8.nextInt();

                                                        try {
                                                            authSystem.removeRole(authSystem.roles.get(chooseRolename-1));
                                                        } catch (Exception e) {
                                                            System.out.println(e);
                                                        }

                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Удаление файла.
                                                case 9:
                                                    try {
                                                        authSystem.viewAllFiles();
                                                        Scanner sc9 = new Scanner(System.in);
                                                        System.out.println("Выберите название файла, который нужно удалить: ");
                                                        int chooseFilename = sc9.nextInt();

                                                        try {
                                                            authSystem.removeFile(authSystem.files.get(chooseFilename-1));
                                                        } catch (Exception e) {
                                                            System.out.println(e);
                                                        }

                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Изменение прав доступа.
                                                case 10:
                                                    try {
                                                        Scanner sc10 = new Scanner(System.in);
                                                        System.out.println("Выберите название файла, в котором нужно изменить права доступа: ");
                                                        authSystem.viewAllFiles();
                                                        System.out.println("Ваш выбор: ");
                                                        int chooseFilename = sc10.nextInt();

                                                        System.out.println("Выберите название роли, для которой нужно изменить права доступа к файлу " + authSystem.files.get(chooseFilename-1).getName());
                                                        authSystem.viewAllRoles();
                                                        System.out.println("Ваш выбор: ");
                                                        int roleNum = sc10.nextInt();

                                                        System.out.print("Параметры разрешений в файле " + authSystem.files.get(chooseFilename-1).getName() + " для роли " + authSystem.roles.get(roleNum-1).getName() + ": ");
                                                        System.out.println(authSystem.getPermissionsForRoleAndFile(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1)));
                                                        System.out.println("Доступные действия:\n" +
                                                                "1. Добавить разрешение READ;\n" +
                                                                "2. Добавить разрешение WRITE;\n" +
                                                                "3. Удалить разрешение READ;\n" +
                                                                "4. Удалить разрешение WRITE;\n" +
                                                                "Выберите действие: ");
                                                        int number10 = sc10.nextInt();
                                                        switch (number10) {
                                                            case 1:
                                                                try {
                                                                    authSystem.setPermissions(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1), Permission.READ);
                                                                    System.out.println("Выполнено!");
                                                                } catch (Exception e) {
                                                                    System.out.println("Выбранного разрешения для удаления не существует");
                                                                }
                                                                break;
                                                            case 2:
                                                                try {
                                                                    authSystem.setPermissions(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1), Permission.READ, Permission.WRITE);
                                                                    System.out.println("Выполнено!");
                                                                } catch (Exception e) {
                                                                    System.out.println("Выбранного разрешения для удаления не существует");
                                                                }
                                                                break;
                                                            case 3:
                                                                try {
                                                                    authSystem.removePermission(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1), Permission.READ);
                                                                    authSystem.removePermission(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1), Permission.WRITE);
                                                                    System.out.println("Выполнено!");
                                                                } catch (Exception e) {
                                                                    System.out.println("Выбранного разрешения для удаления не существует");
                                                                }
                                                                break;
                                                            case 4:
                                                                try {
                                                                    authSystem.removePermission(authSystem.files.get(chooseFilename-1).getName(), authSystem.roles.get(roleNum-1), Permission.WRITE);
                                                                    System.out.println("Выполнено!");
                                                                } catch (Exception e) {
                                                                    System.out.println("Выбранного разрешения для удаления не существует");
                                                                }
                                                                break;
                                                            default:
                                                                System.out.println("Выбранного действия не существует!");
                                                                break;
                                                        }
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Список пользователей.
                                                case 11:
                                                    try {
                                                        authSystem.viewAllUsers();
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Список ролей.
                                                case 12:
                                                    try {
                                                        authSystem.viewAllRoles();
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Список файлов.
                                                case 13:
                                                    try {
                                                        authSystem.viewAllFiles();
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Таблица разрешений.
                                                case 14:
                                                    authSystem.printAccessTable();
                                                    break;

                                                // Список количественных ограничений для каждой роли.
                                                case 15:
                                                    authSystem.viewingQuantityOfRoles();
                                                    break;

                                                // Прочитать файл.
                                                case 16:
                                                    Scanner sc15 = new Scanner(System.in);
                                                    authSystem.viewAllFiles();
                                                    System.out.println("Выберите имя файла для чтения:");
                                                    int numChooseFileRead = sc15.nextInt();
                                                    if (authSystem.files.get(numChooseFileRead-1) != null) {
                                                        authSystem.readFile(user, authSystem.files.get(numChooseFileRead-1));
                                                    } else {
                                                        System.out.println("У Вас нет доступа к файлу " + authSystem.files.get(numChooseFileRead-1).getName());
                                                    }
                                                    break;

                                                // Записать в файл.
                                                case 17:
                                                    Scanner sc16 = new Scanner(System.in);
                                                    authSystem.viewAllFiles();
                                                    System.out.println("Выберите имя файла для записи:");
                                                    int numChooseFileWrite = sc16.nextInt();
                                                    if (authSystem.files.get(numChooseFileWrite-1) != null) {
                                                        authSystem.writeFile(user, authSystem.files.get(numChooseFileWrite-1));
                                                    } else {
                                                        System.out.println("У Вас нет доступа к файлу " + authSystem.files.get(numChooseFileWrite-1));
                                                    }
                                                    break;

                                                // Выйти из системы.
                                                case 18:
                                                    isAdmin = false; // Сбросить флаг администратора
                                                    System.out.println("Выход из системы администратора выполнен!");
                                                    break;

                                                // Завершить сеанс.
                                                case 19:
                                                    System.exit(0);
                                            }
                                        } while (num <= 18 && num > 0 && isAdmin == true);
                                    } else {
                                        do {
                                            System.out.print("""
                                                        __________________________________________________________________________________________________________________
                                                        1) Создать файл;
                                                        2) Прочитать файл;
                                                        3) Записать в файл;
                                                        4) Выйти из системы;
                                                        5) Завершить сеанс.
                                                        __________________________________________________________________________________________________________________
                                                        Выберите действие:\s""");
                                            num = sc.nextInt();
                                            switch (num) {
                                                // Создание файла с автоустановкой прав rw для создателя.
                                                case 1:
                                                    try {
                                                        Scanner sc1 = new Scanner(System.in);
                                                        System.out.println("Придумайте название новому файлу: ");
                                                        String newFileName = sc1.nextLine();
                                                        authSystem.addFile(newFileName);
                                                        authSystem.createFile(newFileName);
                                                        authSystem.setPermissions(newFileName, selectedRole, Permission.READ, Permission.WRITE);
                                                        authSystem.setPermissions(newFileName, authSystem.identifyRoleAdmin(), Permission.READ, Permission.WRITE);
                                                    } catch (Exception e) {
                                                        System.out.println(e);
                                                    }
                                                    break;

                                                // Чтение из файла.
                                                case 2:
                                                    Scanner sc2 = new Scanner(System.in);
                                                    authSystem.viewAllFiles();
                                                    System.out.println("Выберите имя файла для чтения:");
                                                    int numChooseFileRead = sc2.nextInt();
                                                    if (authSystem.files.get(numChooseFileRead-1) != null) {
                                                        authSystem.readFile(user, authSystem.files.get(numChooseFileRead-1));
                                                    } else {
                                                        System.out.println("У Вас нет доступа к файлу " + authSystem.files.get(numChooseFileRead-1).getName());
                                                    }
                                                    break;

                                                // Запись из файла.
                                                case 3:
                                                    Scanner sc3 = new Scanner(System.in);
                                                    authSystem.viewAllFiles();
                                                    System.out.println("Выберите имя файла для записи:");
                                                    int numChooseFileWrite = sc3.nextInt();
                                                    if (authSystem.files.get(numChooseFileWrite-1) != null) {
                                                        authSystem.writeFile(user, authSystem.files.get(numChooseFileWrite-1));
                                                    } else {
                                                        System.out.println("У Вас нет доступа к файлу " + authSystem.files.get(numChooseFileWrite-1));
                                                    }
                                                    break;

                                                // Выход из системы.
                                                case 4:
                                                    num = 0;
                                                    System.out.println("Выход из системы " + selectedRole.getName() + " выполнен!");
                                                    break;

                                                // Завершение сеанса.
                                                case 5:
                                                    System.exit(0);
                                            }
                                        } while (num <= 4 && num > 0);

                                    }
                                } else {
                                    authSystem.incrementLoginAttempts();
                                    System.out.println("Ошибка аутентификации. Попробуйте еще раз!");
                                    System.out.println("У вас осталось попыток: " + (3 - authSystem.getLoginAttempts()));
                                    if (authSystem.getLoginAttempts() >= 3) {
                                        System.out.println("Превышено количество попыток. Подождите некоторое время перед следующей попыткой.");
                                        Thread.sleep(5000);
                                        authSystem.resetLoginAttempts();
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            break;
                        case 2:
                            System.exit(0);
                    }

                } while (number != 2 || !isAdmin);
            } catch (InputMismatchException ime) {
                System.out.println("Введенный Вами символ не является int!");
            }
        }
    }


}
