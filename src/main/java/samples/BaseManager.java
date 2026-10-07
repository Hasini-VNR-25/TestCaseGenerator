package samples;

public abstract class BaseManager {
    protected final String managerName;
    protected boolean active;

    public BaseManager(String managerName) {
        if (managerName == null || managerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Manager name cannot be null or empty");
        }
        this.managerName = managerName;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getManagerName() {
        return managerName;
    }

    public abstract String getStatusReport();
}
