package com.dal.services;

import com.dal.exception.NetworkInputException;
import com.dal.models.InputFrame;

import java.util.*;

public class CommandTextUtilities {

    List<String> allowedOps;
    private static final String RANDOM_STRING_SOURCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random random = new Random();

    public CommandTextUtilities() {
        allowedOps = new ArrayList<>();
        allowedOps.add("AUTH");
        allowedOps.add("LOGOUT");
        allowedOps.add("LIST");
        allowedOps.add("NEW");
        allowedOps.add("ADD");
        allowedOps.add("ORDER");
        allowedOps.add("DROP");
    }

    public InputFrame splitCommand(List<String> commandText) throws NetworkInputException {
        InputFrame inputFrame = new InputFrame();
        Map<String, String> headerValues = new HashMap<>();
        try {
            String operation = commandText.get(0).split("\\s+")[0];
            if (!allowedOps.contains(operation)) {
                throw new NetworkInputException("Invalid Operation");
            }
            inputFrame.setOperation(operation);

            String target = commandText.get(0).split("\\s+")[1];
            inputFrame.setTarget(target);

            String version = commandText.get(0).split("\\s+")[2];
            inputFrame.setProtocol(version);

            for (int i = 1; i < commandText.size(); i++) {
                headerValues.put(
                        commandText.get(i).split(":")[0],
                        commandText.get(i).split(":")[1]
                );
            }
            inputFrame.setHeaders(headerValues);
            return inputFrame;

        } catch (Exception ex) {
            throw new NetworkInputException("Something went wrong");
        }
    }

    public String generateRandom(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * RANDOM_STRING_SOURCE.length());
            builder.append(RANDOM_STRING_SOURCE.charAt(character));
        }
        return builder.toString();
    }
}
