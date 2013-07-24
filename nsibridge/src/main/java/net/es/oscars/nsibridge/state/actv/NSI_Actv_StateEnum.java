package net.es.oscars.nsibridge.state.actv;


public enum NSI_Actv_StateEnum {
    INACTIVE("INACTIVE"),
    SHOULD_ACTIVATE("MUST_ACTIVATE"),
    ACTIVATING("ACTIVATING"),
    ACTIVE("ACTIVE"),
    SHOULD_DEACTIVATE("MUST_DEACTIVATE"),
    DEACTIVATING("DEACTIVATING");

    private final String value;

    NSI_Actv_StateEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NSI_Actv_StateEnum fromValue(String v) {
        for (NSI_Actv_StateEnum c: NSI_Actv_StateEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    }
