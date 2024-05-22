import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreServer extends Thread {
    private static Map<String, Integer> inventory = new HashMap<>();
    private static Map<String, Customer> customers = new HashMap<>();
    private Socket socket;
    private Customer currentCustomer;

    public StoreServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String input = dataInputStream.readUTF();
                 if(input.equals("close")) {
                     socket.close();
                     break;
                 } else if (ValidInputs.REGISTER.isMatch(input)) {
                    String id = ValidInputs.REGISTER.getGroup(input, "id");
                    String name = ValidInputs.REGISTER.getGroup(input, "name");
                    String moneyStr = ValidInputs.REGISTER.getGroup(input, "money");
                    registerHandler(id, name, moneyStr, dataOutputStream);
                } else if (ValidInputs.LOGIN.isMatch(input)) {
                    String id = ValidInputs.LOGIN.getGroup(input, "id");
                    loginHandler(id, dataOutputStream);
                } else if (ValidInputs.GET_PRICE.isMatch(input)) {
                    String productName = ValidInputs.GET_PRICE.getGroup(input, "shoename");
                    if (StoreController.isValidProductName(productName)) {
                        dataOutputStream.writeUTF(productName + " has price : " + String.valueOf(StoreController.getPrice(productName)));
                    } else {
                        dataOutputStream.writeUTF("invalid product name.");
                    }
                } else if (ValidInputs.GET_QUANTITY.isMatch(input)) {
                    String productName = ValidInputs.GET_QUANTITY.getGroup(input, "shoename");
                    if (StoreController.isValidProductName(productName)) {
                        dataOutputStream.writeUTF(productName + " has quantity : " + String.valueOf(getQuantity(productName)));
                    } else {
                        dataOutputStream.writeUTF("invalid product name");
                    }
                } else if (ValidInputs.GET_MONEY.isMatch(input)) {
                    getCustomerMoney(dataOutputStream);
                } else if (ValidInputs.CHARGE.isMatch(input)) {
                    String moneyStr = ValidInputs.CHARGE.getGroup(input, "money");
                    chargeHandler(moneyStr, dataOutputStream);
                } else if (ValidInputs.PURCHASE.isMatch(input)) {
                    String productName = ValidInputs.PURCHASE.getGroup(input, "shoename");
                    String quantityStr = ValidInputs.PURCHASE.getGroup(input, "quantity");
                    purchaseHandler(productName, quantityStr, dataOutputStream);
                } else if (ValidInputs.LOGOUT.isMatch(input)) {
                     if(currentCustomer == null) {
                         dataOutputStream.writeUTF("nobody is logged in");
                         continue;
                     }
                    currentCustomer = null;
                    dataOutputStream.writeUTF("logged out successfully");
                    System.out.println("logged out successfully\ncurrent customer is null");
                } else {
                    dataOutputStream.writeUTF("invalid input");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargeHandler(String moneyStr, DataOutputStream dataOutputStream) throws IOException {
        if(currentCustomer == null) {
            dataOutputStream.writeUTF("please first log in and then try to charge your account.");
            return;
        }
        if (StoreController.isValidMoney(moneyStr)) {
            int chargeAmount = Integer.parseInt(moneyStr);
            chargeCustomer(chargeAmount, dataOutputStream);
            dataOutputStream.writeUTF("charged successfully\nyour new balance is: " + currentCustomer.getMoney());
            System.out.println("customer : " +currentCustomer.getName() + " & balance : " + currentCustomer.getMoney());
        } else {
            dataOutputStream.writeUTF("invalid input\nplease enter a valid number");
            System.out.println("invalid input");
        }
    }

    private void purchaseHandler(String productName, String quantityStr, DataOutputStream dataOutputStream) throws IOException {
        if(!StoreController.isValidProductName(productName)) {
            dataOutputStream.writeUTF("invalid product name");
            return;
        } else if(!StoreController.isValidQuantity(quantityStr)) {
            dataOutputStream.writeUTF("invalid quantity\nplease enter a valid number");
            return;
        } else if(currentCustomer == null) {
            dataOutputStream.writeUTF("nobody is logged in\n please first log in and then purchase");
            return;
        } else {
            int quantity = Integer.parseInt(quantityStr);
            if(currentCustomer.getMoney() < quantity * StoreController.getPrice(productName)) {
                dataOutputStream.writeUTF("not enough money");
                return;
            } else if(inventory.get(productName) < quantity) {
                dataOutputStream.writeUTF("not enough quantity");
                return;
            } else {
                purchaseProduct(productName, quantity, dataOutputStream);
            }
        }
    }

    private synchronized void registerHandler(String id, String name, String moneyStr, DataOutputStream dataOutputStream) throws IOException {
        if(!StoreController.isValidId(id)) {
            dataOutputStream.writeUTF("invalid id\nplease enter a valid id");
            System.out.println("invalid id");
            return;
        } else if(!StoreController.isValidName(name)) {
            dataOutputStream.writeUTF("invalid name\nplease enter a valid name");
            System.out.println("invalid name");
            return;
        } else if(!StoreController.isValidMoney(moneyStr)) {
            dataOutputStream.writeUTF("invalid money\nplease enter a valid number");
            System.out.println("invalid money");
            return;
        } else if(customers.containsKey(id)) {
            dataOutputStream.writeUTF("id already exists, please choose another id");
            System.out.println("invalid id");
            return;
        } else {
            int money = Integer.parseInt(moneyStr);
            customers.put(id, new Customer(name, id, money));
            dataOutputStream.writeUTF("registered successfully");
            System.out.println("added customer " + name + " with id " + id + " and money " + money);
        }

    }
    private void loginHandler(String id, DataOutputStream dataOutputStream) throws IOException {
        if(!StoreController.isValidId(id)) {
            dataOutputStream.writeUTF("invalid id\nplease enter a valid id");
            System.out.println("invalid id");
        } else if(customers.get(id) == null) {
            dataOutputStream.writeUTF("customer not found");
            System.out.println("customer not found");
            return;
        }else {
            setCurrentCustomer(customers.get(id));
            dataOutputStream.writeUTF("logged in successfully");
            System.out.println("customer " + currentCustomer.getName() + " logged in");
        }
    }
    private void chargeCustomer(int chargeAmount, DataOutputStream dataOutputStream) throws IOException {
        try {
            currentCustomer.setMoney(currentCustomer.getMoney() + chargeAmount);
            dataOutputStream.writeUTF("charged successfully\nyour balance is now : " + currentCustomer.getMoney());
        } catch (Exception e) {
            dataOutputStream.writeUTF("invalid input");
        }
    }
    private void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }
    private int getQuantity(String productName) {
        return inventory.get(productName);
    }

    private synchronized void purchaseProduct(String productName, int quantity, DataOutputStream dataOutputStream) throws IOException {
        try {
            if (inventory.get(productName) >= quantity) {
                inventory.put(productName, inventory.get(productName) - quantity);
                currentCustomer.setMoney(currentCustomer.getMoney() - quantity * StoreController.getPrice(productName));
                dataOutputStream.writeUTF("purchased successfully");
            } else {
                dataOutputStream.writeUTF("not enough quantity");
            }
        } catch (Exception e) {
            dataOutputStream.writeUTF("invalid input");
        }
    }

    private void getCustomerMoney(DataOutputStream dataOutputStream) throws IOException {
        try {
            dataOutputStream.writeUTF("your balance is: " + currentCustomer.getMoney());
        } catch (Exception e) {
            dataOutputStream.writeUTF("nobody is logged in");
        }
    }

    public static void main(String[] args) throws IOException {
        inventory.put("shoe1", 5);
        inventory.put("shoe2", 5);
        inventory.put("shoe3", 5);
        inventory.put("shoe4", 5);
        try (ServerSocket storeServer = new ServerSocket(8080)) {
            while (true) {
                Socket clientSocket = storeServer.accept();
                new StoreServer(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
