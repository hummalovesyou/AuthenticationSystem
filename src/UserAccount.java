import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class UserAccount implements Serializable {
    private String username;
    private String password;
    private List<Roles> roles;
    private int lengthPassword;

    private Roles role;
    private static List<UserAccount> allUsers = new ArrayList<>();



    // Конструктор для пользователя без роли
    public UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<>();
        allUsers.add(this); // Добавляем пользователя в список при создании
    }

    // Конструктор для пользователя с произвольной ролью
    public UserAccount(String username, String password, Roles role) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<>();
        if (role!= null) this.roles.add(role);

        allUsers.add(this); // Добавляем пользователя в список при создании
    }

    public void chooseRole (int num){
        this.role = roles.get(num);
    }

    // Добавить роль пользователю
    public void addRole(Roles role) {
        if (role != null && !roles.contains(role)) {
            this.roles.add(role);
        }
    }

    // UserAccount
    public boolean isEmpty() {
        // Проверяем, что список ролей не пуст и все объекты Roles не являются null
        return roles.isEmpty() || roles.stream().allMatch(role -> role == null);
    }

    public void viewRoles() {
        if (isEmpty()) {
            System.out.println("У данного пользователя нет ролей!");
        } else {
            for (Roles role : roles) {
                if (role != null) {
                    System.out.println(role.getName());
                }
            }
        }
    }

    // Удалить роль у пользователя
    public void removeRole(Roles role) {
        if (role != null) {
            roles.remove(role);
        }
    }

    public void setLengthPassword (int length) {lengthPassword = length;}
    public int getLengthPassword (){return lengthPassword;}
    public String getUsername() {
        return username;
    }

    public void setPassword (String newPass){
        this.password = newPass;
    }

    public String getPassword() {
        return password;
    }

    public Roles getRole() {
        return role;
    }

    public List<Roles> getRoles() {
        return roles;
    }

    // Возвращает строку со всеми ролями пользователя
    public String strUserRoles() {
        String allRolesStr = "";
        for (Roles role : roles) {
            if (role != null) {
                allRolesStr += role.getName() + " ";
            }
        }
        return allRolesStr;
    }

    public String toStringTwo(){return username;}

    @Override
    public String toString() {
        return "Логин: " + username + ", пароль: " + password + ", роли: " + strUserRoles();
    }

    public int hashCode() {
        return Objects.hash(username);
    }
}