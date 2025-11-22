package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> chatArrayAdapter;
    private List<String> chatMessages;

    private BluetoothDevice selectedDevice;
    private EditText messageEditText;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private Button sendButton;
    private Button listDevicesButton;

    protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_PERMISSIONS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBluetooth();
        checkPermissions();
        startServer();
        sendMessage();

        // TODO 1: Initialize UI components
        // - Find views by ID: chatListView, messageEditText, sendButton, listDevicesButton
        // - Initialize chatMessages list and chatArrayAdapter
        // - Set adapter to chatListView

        // TODO 2: Initialize Bluetooth adapter
        // - Get default Bluetooth adapter
        // - Check if Bluetooth is supported (if null, show toast and finish)

        // TODO 3: Request necessary permissions
        // - Call requestPermissions() method

        // TODO 4: Start server socket to listen for incoming connections
        // - Create and start AcceptThread

        // TODO 5: Set up button listeners
        // - listDevicesButton should call listPairedDevices()
        // - sendButton should get message from EditText, send it via connectedThread,
        //   clear EditText, and add message to chat
    }

    private void initViews() {
        // TODO 1: Implement the method that initializes the views
        // Conectăm componentele din XML la codul Java
        ListView chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        listDevicesButton = findViewById(R.id.listDevicesButton);

        // Setăm un eveniment pentru butonul de listare a dispozitivelor
        // apeleaza listPairedDevices() cand butonul este apasat
        listDevicesButton.setOnClickListener(v -> listPairedDevices());

        // Pregătim o listă pentru mesaje
        chatMessages = new ArrayList<>();

        // Creăm un adaptor care transformă lista noastră de mesaje în elemente vizuale
        chatArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);

        // Schimbarile din adaptor se vor vedea imediat in interfata grafica. In acest exemplue, chatListView de tip ListView va fi actualizata cand facem schimbari la chatArrayAdapter
        chatListView.setAdapter(chatArrayAdapter);
    }

    private void initBluetooth() {
        // Vom lua o referinta la adaptorul de bluetooth
        // de pe telefon. Putem vedea acest adaptor ca o interfata
        // cu driver-ul de bluetooth.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verificăm dacă Bluetooth este disponibil
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available.", Toast.LENGTH_LONG).show();
            finish(); // Închidem aplicația dacă Bluetooth nu este disponibil
        }
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        // Pentru Android 12 sau mai nou, folosim permisiuni specifice Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        } else {
            // Pentru versiuni mai vechi, accesul la locație este necesar
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Cerem permisiunile utilizatorului
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // TODO 4: handle permission results
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean permissionGranted = true;

            // Verificăm dacă toate permisiunile au fost acordate
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }

            if (!permissionGranted) {
                Toast.makeText(this, "Permissions required for Bluetooth operation.", Toast.LENGTH_LONG).show();
                finish();
            }

            // Activăm Bluetooth dacă nu este deja activat
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    // Handle Bluetooth enable result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
            Toast.makeText(this, "Bluetooth must be enabled to continue.", Toast.LENGTH_LONG).show();
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_PERMISSIONS);
    }

    private void listPairedDevices() {
        // TODO 5: Implement the method that displays a dialog for selecting a paired device
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Obținem dispozitivele împerecheate
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<String> deviceList = new ArrayList<>();
        final List<BluetoothDevice> devices = new ArrayList<>();

        // Le adaugam in lista
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
        }

        // Afișăm dispozitivele într-un dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device");

        ArrayAdapter<String> deviceArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);

        // Tratam situatia in care un dispozitiv este apasat in cadrul dialogului
        builder.setAdapter(deviceArrayAdapter, (dialog, which) -> {
            selectedDevice = devices.get(which);

            // deschidem un thread de comunicare
            connectThread = new ConnectThread(this, bluetoothAdapter, selectedDevice, MY_UUID);
            connectThread.start();
        });

        builder.show();
    }

    private void startServer() {
        // TODO 6: Implement server socket to listen for incoming connections
        // Inițializăm AcceptThread, care va gestiona conexiunile primite
        acceptThread = new AcceptThread(this, bluetoothAdapter, MY_UUID);
        acceptThread.start();
    }

    private void sendMessage() {
        // TODO 7: Implement the method that sends a message to the connected device
        sendButton.setOnClickListener(v -> {
            // Preluăm mesajul introdus de utilizator
            String message = messageEditText.getText().toString();

            // Verificăm dacă mesajul nu este gol și conexiunea este activă
            if (!message.isEmpty() && connectedThread != null) {

                // Trimitem mesajul ca un array de bytes
                connectedThread.write(message.getBytes());

                // Golește câmpul de text
                messageEditText.setText("");

                // Adaugă mesajul trimis în interfață
                addChatMessage("Me: " + message);
            }
        });
    }

    public void addChatMessage(String message) {
        chatMessages.add(message); // Adaugă mesajul în listă
        chatArrayAdapter.notifyDataSetChanged(); // Actualizează interfața
    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        connectedThread = new ConnectedThread(this, socket);
        connectedThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO 8: cleanup threads
        // Închidem AcceptThread dacă rulează
        if (acceptThread != null) acceptThread.cancel();

        // Închidem ConnectThread dacă rulează
        if (connectThread != null) connectThread.cancel();

        // Închidem ConnectedThread dacă rulează
        if (connectedThread != null) connectedThread.cancel();
    }

}
