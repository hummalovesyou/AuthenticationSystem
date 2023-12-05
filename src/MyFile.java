import java.io.Serializable;
import java.util.*;


// Класс для хранения информации о файлах
class MyFile implements Serializable {
    private String name;
    private Map<Roles, Set<Permission>> rolePermissions;
    private static List<MyFile> allFiles = new ArrayList<>(); // Список для хранения всех файлов

    public MyFile(String name) {
        this.name = name;
        this.rolePermissions = new HashMap<>();
        allFiles.add(this); // Добавляем созданный файл в список
    }

    public static MyFile getFileByName(String roleName) {
        for (MyFile file : allFiles) {
            if (file.getName().equals(roleName)) {
                return file;
            }
        }
        return null; // Роль не найдена
    }

    public Map<Roles, Set<Permission>> getRolePermissions() {
        return rolePermissions;
    }

    // Метод для добавления разрешений для роли к файлу
    public void addPermissions(Roles role, Permission... permissions) {
        Set<Permission> permissionSet = rolePermissions.computeIfAbsent(role, k -> new HashSet<>());
        permissionSet.addAll(Arrays.asList(permissions));
    }

    // Метод для удаления разрешения для роли к файлу
    public void removePermission(Roles role, Permission permission) {
        Set<Permission> permissions = rolePermissions.get(role);
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    // Метод для проверки, может ли роль читать файл
    public boolean canRead(Roles role) {
        Set<Permission> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(Permission.READ);
    }

    // Метод для проверки, может ли роль писать в файл
    public boolean canWrite(Roles role) {
        Set<Permission> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(Permission.WRITE);
    }

    public String getName() {
        return name;
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
        MyFile other = (MyFile) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
