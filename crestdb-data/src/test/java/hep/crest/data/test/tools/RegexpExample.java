package hep.crest.data.test.tools;

public class RegexpExample {

    public static void main(String[] args) {
        final String aname = "A-TEST-02";
        final boolean islike = aname.matches("A-TEST.*");
        System.out.println("Found matching "+islike+" for  "+aname);
    
    }
}
