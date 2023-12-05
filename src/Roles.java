import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


class Roles implements Serializable {
    private String name; // Имя роли.
    private int quantity = Integer.MAX_VALUE; // Максимальное количество участников с данной ролью.
    private int count; // Количество существующих участников с данной ролью
    private boolean isAdmin; // Флаг администратора.
    public static List<Roles> allRoles = new ArrayList<>(); // Список для хранения всех ролей.

    // Конструктор класса для роли с количественным ограничением на назначение пользователям.
    public Roles(String name, int quantity){
        this.name = name;
        this.quantity = quantity;
        this.count = 0;
        if (name.equals("Проектный руководитель")) this.isAdmin = true;
        allRoles.add(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCount () {
        return count;
    }

    public boolean upCount () {
        if (count < quantity) {
            this.count++;
            return true;
        }
        return false;
    }

    public boolean reduceCount () {
        if (count > 0) {
            this.count--;
            return true;
        }
        return false;
    }

    public boolean isCorrectToAdd (){
        if (getCount() < getQuantity()) return true;
        return false;
    }

    public void viewingQuantityOfARole(){
        System.out.println("Заполненность роли: " + this.count + "/" + this.quantity + ".");
    }

    public void viewingQuantityOfRoles(){
        for (Roles role : allRoles){
            System.out.println("Заполненность роли " + role.getName() + ": " + role.getCount() + "/" + role.getCount() + ".");
        }
    }

    // Проверка на существование роли по имени.
    public static Roles getRoleByName(String roleName) {
        for (Roles role : allRoles) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null; // Роль не найдена.
    }

    public boolean isAdmin () {
        return isAdmin;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Roles other = (Roles) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static void removeRole(Roles role) {
        if (role != null) {
            System.out.println("Роль " + role.getName() + " удалена!");
                allRoles.remove(role);
            } else {
                System.out.println("Роль не найдена!");
            }
        }
    }

