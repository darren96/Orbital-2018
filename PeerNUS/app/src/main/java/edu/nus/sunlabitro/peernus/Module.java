package edu.nus.sunlabitro.peernus;

public class Module {

    private int id;
    private String moduleCode;
    private String moduleTitle;
    private int[] semesters;

    public Module() {

    }

    public Module(int id, String moduleCode, String moduleTitle) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.moduleTitle = moduleTitle;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleTitle() {
        return moduleTitle;
    }

    public int getId() {
        return id;
    }
}
