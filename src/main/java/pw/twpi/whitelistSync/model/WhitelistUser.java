package pw.twpi.whitelistSync.model;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class WhitelistUser {

    private final String uuid;
    private final String name;

    public WhitelistUser(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public WhitelistUser(WhitelistUser whitelistUser) {
        this.uuid = whitelistUser.getUuid();
        this.name = whitelistUser.getName();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "WhitelistUser{" + "uuid=" + uuid + ", name=" + name + '}';
    }

}
