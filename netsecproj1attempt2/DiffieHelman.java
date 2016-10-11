package netsecproj1attempt2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;

public class DiffieHelman {
    private long p; //A large prime number
    private long a; //This is a primitive root mod p
    private long x; //Secret number 1 <= x <= (p - 2)
    private long o; //The shared key
            
    public DiffieHelman () { //Set small numbers that won't really work. They should be changed.
        p = 7;
        a = 3;
        x = 5;
        o = -1;
    }
    
    /**
     * Would not recommend supplying this method with a value over 32 bits in
     * length. Less depending on your processing power.
     * 
     * @param prim A prime number, this method assumes you will provide it with
     * a number that is prime or at least probably prime
     * @return a primitive root modulo prim
     */
    private long primitiveRoot(long prim) {
        //We assume the number we are given is prime.
        //Then, by Euler's totient function, prim has prim - 1 relative primes
        //We say s = eulersTotient(prim) and, assuming prim is prim, s = prim - 1
        long s = prim - 1;
        
        //Next, we need to collect all the numbers that are prime and that divide s
        //that is, we we need to know the prime factors of s
        //Yes, this is a process and why this method should not be given too
        //large a number
        double percDone = -5;
        ArrayList<Long> primeFactors = new ArrayList<>();
        for (long i = 2; i <= (s / 2); i ++) {
           if ((s % i == 0) && new BigInteger(Long.valueOf(i).toString()).isProbablePrime(10)) {
               primeFactors.add(i);
           }
           if ((((double)i / (s / 2)) * 100) >= (percDone + 5)) {
               percDone = (((double)i / (s / 2)) * 100);
               System.out.println(percDone + "% complete");
           }
        }
        System.out.println("Complete.");
        
        //Now, we have all the (probably) prime factors of s. We divide s by its
        //prime factors and remember those numbers
        ArrayList<Long> nums = new ArrayList<>();
        for (Long item : primeFactors) {
            nums.add(s / item);
        }
        
        //Now we have what we need to find the lowest primitive root.
        //We simply begin from x = 2 and check every number for:
        //x ^ valuesInnums % prim
        //If we get one, our number is not a primitive root module prim
        //If we do not get one, we have found a primitive root
        boolean isPrimRoot;
        long primRoot = -1; //Error
        for (long i = 2; i < prim; i++) {
            isPrimRoot = true;
            for (Long item : nums) {
                if (expSqu(i, item, prim) == 1) {
                    isPrimRoot = false;
                    break;
                }
            }
            if (isPrimRoot) {
                primRoot = i;
                break;
            }
        }
        
        //Now, we have our lowest primitive root mod prim. We can use this to
        //find all other primitive roots mod prim, however, this is a resource
        //intensive operation, as we have to compute primRoot ^ m % prim for all
        //1 <= m < prim and determine whether or not the gcd of this value and s
        //is one.
        //Though it will (I believe) significantly weaken the strength of our
        //key exchange, we're going to stick with using our smallest primitive
        //root
        
        return primRoot;
    }
    
    
    private long generateRandomLong () {
        long LOWER_RANGE = 0; //Assign lower range value
        long UPPER_RANGE = 0x7FFFFFFFFFFFFFFFL; //Assign upper range value
        Random random = new Random();
        return (LOWER_RANGE + (long)(random.nextDouble() * (UPPER_RANGE - LOWER_RANGE))) - 1;
    }
    
    /* Included because I don't really want to delete them.
    private long generateRandomLong (long UPPER_RANGE) {
        long LOWER_RANGE = 0; //assign lower range value
        Random random = new Random();
        return (LOWER_RANGE + (long)(random.nextDouble() * (UPPER_RANGE - LOWER_RANGE))) - 1;
    }
    
    private long generateRandomLong (long LOWER_RANGE, long UPPER_RANGE) {
        Random random = new Random();
        return (LOWER_RANGE + (long)(random.nextDouble() * (UPPER_RANGE - LOWER_RANGE))) - 1;
    }
    */
    
    //Generate a large psuedo-prime number
    private long primeMaker (int bitLength) {
        return BigInteger.probablePrime(bitLength, new SecureRandom()).longValue();
    }
    
    //computeX() with a parameter, they sent us an x they want to use.
    public void computeX (long x) {
        /* x must be a number such that 1 <= x <= (p - 2)
           So if their number is !(1 <= x <= (p - 2)), we'll force it into
           range.
        */
        if ((x > (p - 2)) || (x < 1)) {
            x = x % (p - 2);
        }
        this.x = x;
    }
    
    //computeX() without parameter, we generate x
    public void computeX () {
        this.x = generateRandomLong();
        if ((this.x > (p - 2)) || (this.x < 1)) { //Force x into range 1 <= x <= (p - 2)
                this.x = this.x % (p - 2);
            }
    }
    
    //computeP() with a parameter, they sent us an p they want to use.
    public void computeP (long p) {
        this.p = p;
        /*if (!millerRabin(p)) {
            computeP()
        }*/
    }
    
    //computeP() without parameter, we generate p
    public void computeP () {
        this.p = primeMaker(32);
    }
      
    //computeA() with a parameter, they sent us an a they want to use.
    public void computeA (long a) {
        this.a = a;
    }
    
    //computeA() without parameter, we generate a
    public void computeA() {
        this.a = primitiveRoot(p);
    }
    
    public String getMyPublicKey () {
        return "Prime " + p + " and primitive root " + a;
    }
    
    public void diffieServer(int port) {
        /*
        //Put our public key in a text file
        Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "utf-8"));
        w.write(String.valueOf(getMyPublicKey()));
        w.close();
        */
        if ((port < 0) || (port > 0xFFFF)) {
            port = 8080;
        }
        
        computeP();
        computeA();
        computeX();
        
        ServerSocket Server = null;
        
        try {
            Server = new ServerSocket(8080);
        }
        catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
        
        //Set to zero in case we don't get any good input
        String clientInput = "0";
        
        //Accepts a connection to a socket.
        Socket connectedSocket = null;
        try {
            //Program waits at this line until a connection is made.
            connectedSocket = Server.accept();
        }
        catch (IOException e) {
            System.err.println("Could not accept connection");
            System.exit(-1);
        }
        System.out.println("Connection accepted on port " + port);
        
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;
        
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
            outToClient = new PrintWriter(connectedSocket.getOutputStream(), true);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException in BufferedReader or PrintWriter");
            System.exit(-1);
        }

        System.out.println("BufferedReader and PrintWriter established.");
        
        System.out.println("PrintWriter writing: " + p);
        outToClient.println(p);
        
        System.out.println("PrintWriter writing: " + a);
        outToClient.println(a);
        
        try {
            clientInput = inFromClient.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading from client");
            System.exit(-1);
        }
        
        System.out.printf("BufferedReader read: %s\n", clientInput);
        
        o = expSqu(Long.valueOf(clientInput).longValue(), x, p);
        
        long send = expSqu(a, x, p);
        
        System.out.println("PrintWriter writing: " + send);
        outToClient.println(send);
        
        try {
            Server.close();
            connectedSocket.close();
        }
        catch (IOException e) {
            System.err.println("Error cleaning up");
            e.printStackTrace();
        }
    }
    
    public void diffieClient(String ip, int port) {
        if ((port < 0) || (port > 0xFFFF)) {
            port = 8080;
        }
        
        Socket socket = null;
        
        try {
            socket = new Socket(ip, port);
        }
        catch (UnknownHostException e) {
            System.err.println("Unable to connect to " + ip + " at " + port);
            e.printStackTrace();
            System.exit(-1);
        }
        catch (IOException e) {
            System.err.println("IOExcpetion");
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Connection established to " + ip + " at " + port);
        
        BufferedReader incoming = null;
        try {
            incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e) {
            System.err.println("Problem creating BufferedReader");
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("BufferedReader established");
        
        try {
            p = Long.valueOf(incoming.readLine()).longValue();
        }
        catch (IOException e) {
            System.err.println("Unable to read from socket");
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Read from socket: " + p);
        
        computeX();
        
        try {
            a = Long.valueOf(incoming.readLine()).longValue();
        }
        catch (IOException e) {
            System.err.println("Unable to read from socket");
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Read from socket: " + a);
        
        long send = expSqu(a, x, p);
        
        try {
            new PrintWriter(socket.getOutputStream(), true).println(send);
        }
        catch (IOException e) {
            System.err.println("Unable to write to socket");
            e.printStackTrace();
            System.exit(-1);
        }        
        
        System.out.println("Write successful. Wrote: " + send);
        
        String lineIn = null;
        try {
            lineIn = incoming.readLine();
        }
        catch (IOException e) {
            System.err.println("Unable to read from socket");
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Read from socket: " + lineIn);
        long fromServer = Long.valueOf(lineIn).longValue();
        
        
        o = expSqu(Long.valueOf(fromServer).longValue(), x, p);
        
        try {
            socket.close();
        }
        catch (IOException e) {
            System.err.println("Problem closing socket");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /*
    public long getPrivateKey() {
        return (long)Math.pow(o, x);
    }
    */
    
    public long getPrivateKey() {
        return o;
    }
    
    //Stolen from http://stackoverflow.com/questions/30694842/exponentiation-by-squaring
    //Exponentiation by Squaring
    public long expSqu(long base, long exp, long mod) {
        if ((base < 1) || (exp < 0) || (mod < 1)) {
            System.out.println("Base: " + base);
            System.out.println("Exponent: " + exp);
            System.out.println("Modulus: " + mod);
            return -1;
        }
        
        long result = 1;
        while (exp > 0) {
            if ((exp % 2) == 1) {
                result = (result * base) % mod;
            }
            base = (base * base) % mod;
            exp = exp / 2;
        }
        return result;
    }
}
