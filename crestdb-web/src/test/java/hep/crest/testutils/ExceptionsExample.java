package hep.crest.testutils;

import hep.crest.data.exceptions.CdbServiceException;
import hep.crest.server.exceptions.AlreadyExistsIovException;
import hep.crest.server.exceptions.AlreadyExistsPojoException;

public class ExceptionsExample {

    public ExceptionsExample() {

    }

    public static void main(String[] args) {

        try {
            System.out.println("Send an Exception for IOV");
            throw new AlreadyExistsIovException("iov is already there");
        }
        catch (AlreadyExistsPojoException e) {
            e.printStackTrace();
            System.out.println("Pojo Exception message : " + e.getMessage());
        }

        try {
            System.out.println("Send an Exception for IOV");
            throw new AlreadyExistsIovException("iov is already there");
        }
        catch (CdbServiceException e) {
            e.printStackTrace();
            System.out.println("Cdb Exception message : " + e.getMessage());
        }

    }
}
