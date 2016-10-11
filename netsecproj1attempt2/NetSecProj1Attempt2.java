package netsecproj1attempt2;

import java.util.Scanner;

public class NetSecProj1Attempt2 {
    
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        int t; 
        DiffieHelman d = new DiffieHelman();
        
        do {
            System.out.print("Enter\n1:\tTo be Client\n2:\tTo be Server\n3:\tExit\n::->  ");
            t = input.nextInt();
            System.out.println();
            switch(t) {
                case 1:
                    d.publicKeyClient("localhost", 8080);
                    break;
                case 2:
                    System.out.println("Calculating your public key ...");
                    d.publicKeyServer(8080);
                    break;
                case 3:
                    System.exit(0);
                default:
            }
        } while ((t != 1) && (t != 2));
        
        
        System.out.printf("Your public key:\t%d =||= 0x%X\n", d.getMyPublicKey(), d.getMyPublicKey());
        System.out.printf("Your private key:\t%d =||= 0X%x\n", d.getPrivateKey(), d.getPrivateKey());
    }
}
