package ai.latta.core;

import ai.latta.core.models.SystemInfo;
import ai.latta.core.models.api.CreateInstance;
import ai.latta.core.models.api.CreateSnapshot;
import ai.latta.core.models.exceptions.BaseException;
import ai.latta.core.models.exceptions.SystemException;

import java.util.Date;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {

        var apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9qZWN0IjoiMWE3NTUyOWItZjUwNi00Yjk5LWI1ZTMtN2Y5ZjY5YzRmZDhmIiwiaWF0IjoxNzI3MjUzNjk5fQ.FejGfhY_Eqgkafd9_GvQrecvV8UuEk5gcT2s0XmPUDU";
        var client = new LattaClient(apiKey);

        var createInstanceData = new CreateInstance("Java", "1.0.0");

        var instance = client.createInstance(createInstanceData).join();
        var snapshot = client.createSnapshot(instance, CreateSnapshot.fromRelation("Message")).join();

        System.out.println(snapshot.id);


        SystemException ex = new SystemException();
        ex.environmentVariables = new HashMap<>(System.getenv());
        ex.level = BaseException.Level.ERROR;
        ex.name = "Test error";
        ex.message = "Message";
        ex.systemInfo = new SystemInfo();
        ex.timestamp = new Date();


        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ex);

        System.out.println(json);

        client.attachRecord(snapshot, ex).join();
    }
}