package amb.server.plugin.service.aip.entity.controller;

import amb.server.plugin.service.aip.entity.Friday;

public class PlayerControllerJump {
    private boolean a;
    private final Friday b;

    public PlayerControllerJump(Friday var0) {
        this.b = var0;
    }
    public void b() {
        this.b.setJumping(this.a);
        this.a = false;
    }

    public void jump() {
        this.a = true;
    }
}
