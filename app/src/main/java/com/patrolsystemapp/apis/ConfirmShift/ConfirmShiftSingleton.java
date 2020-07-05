package com.patrolsystemapp.Apis.ConfirmShift;

import com.patrolsystemapp.Apis.ConfirmShiftRequest;

import java.io.Serializable;
import java.util.ArrayList;

public class ConfirmShiftSingleton implements Serializable {
    private static ArrayList<ConfirmShiftRequest> confirmShiftRequests = new ArrayList<>();
    private static final ConfirmShiftSingleton ourInstance = new ConfirmShiftSingleton();

    static ConfirmShiftSingleton getInstance() {
        return ourInstance;
    }

    private ConfirmShiftSingleton() {
    }

    public static ArrayList<ConfirmShiftRequest> getConfirmShiftRequests() {
        return confirmShiftRequests;
    }

    //Make singleton from serialize and deserialize operation.
    protected ConfirmShiftSingleton readResolve() {
        return getInstance();
    }
}
