package com.example.helpme;

import static com.example.helpme.extras.IntentExtras.CHAT_SELECCIONADO;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpme.model.Alumno;
import com.example.helpme.model.Chat;
import com.example.helpme.model.Mensaje;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.MensajeAdapter;
import chat.AlumnoStatus;
import chat.ChatService;
import chat.MensajeStatus;
import dto.AlumnoDto;
import dto.ChatSummaryDto;
import dto.MensajeDto;
import util.ContentTypeUtils;
import util.DateUtils;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "CHAT_ACTIVITY";

    /* Identicar intent que accede a la cámara del dispositivo */
    public static final int CAMERA_IMAGE_CHAT = 1000;
    public static final int ADJUNTO_CHAT_REQUEST_CODE = 1001;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1002;

    private EditText txMensajeAEnviar;
    private ImageButton btEnviarMensaje;

    private ImageView imgPerfilUsuarioReceiver;
    private TextView txNombreUsuarioReceiver;
    private TextView txStatusUsuarioReceiver;

    private ImageButton btSubirArchivoChat;
    private ImageButton btVolverListaChats;
    private ImageButton btLlamarReceiver;
    private ImageButton btCamera;

    private RecyclerView recyclerConversacionChat;

    private DatabaseReference dbReference = FirebaseDatabase.getInstance(ChatService.DB_URL).getReference();
    private FirebaseUser userInSession = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore dbStore = FirebaseFirestore.getInstance();

    private AlumnoDto alumnoB = new AlumnoDto();

    private ChatSummaryDto originChatDataDto;

    private MensajeAdapter msgAdapter;
    private List<MensajeDto> chatMessages = new ArrayList<>();

    private Bitmap selectedImageToSend;

    /* Permisos grabar audio */
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    /* Grabacion de audio */
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    private boolean grabandoAudio = false;

    private String audioOutputFileName = null;

    /* Online, Offline, Escribiendo...*/
    private static final String ONLINE_STATUS = AlumnoStatus.ONLINE.toString().toLowerCase();
    private static final String OFFLINE_STATUS = AlumnoStatus.OFFLINE.toString().toLowerCase();
    private static final String TYPING_STATUS = AlumnoStatus.ESCRIBIENDO.toString().toLowerCase();
    private String status = ONLINE_STATUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        if (savedInstanceState == null && userInSession != null) {
            Bundle info = getIntent().getExtras();
            processIntentExtras(info);

        } else {
            Toast.makeText(this, "Inicia sesión", Toast.LENGTH_SHORT).show();

            //AlumnoDto alumno = (AlumnoDto) savedInstanceState.getSerializable(ListadoAlumnosChatActivity.ALUMNO_SELECCIONADO_CHAT);
            //Log.i(TAG, "ALUMNO-DTO-SAVED-INSTANCE-NN: " + alumno.toString());
        }


        initFields();

        /* Accion enviar mensaje */
        btEnviarMensaje.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Enviando mensaje...");

//                if (grabandoAudio == false) {
//                    startRecording();
//                }

                MensajeDto newMsgDto = new MensajeDto();
                newMsgDto.contenido = txMensajeAEnviar.getText().toString();
                newMsgDto.createdAt = DateUtils.getNowWithPredefinedFormat();
                newMsgDto.status = MensajeStatus.ENVIADO;

                Log.d(TAG, originChatDataDto.receiverUid + " " + userInSession.getUid());

                if (originChatDataDto != null) {
                    ChatService.getInstance().sendMessage(newMsgDto, originChatDataDto, userInSession.getUid(), new ChatService.MensajeCallback() {
                        @Override
                        public void callback() {
                            txMensajeAEnviar.setText("");
                            recyclerConversacionChat.smoothScrollToPosition(View.FOCUS_DOWN);
                        }
                    });
                }
            }
        });

//        btEnviarMensaje.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_UP: {
//                        grabandoAudio = false;
//                        //stop recording voice if a long hold was detected and a recording started
//                        detenerGrabacionAudio();
//                        Log.d(TAG, "Grabacion parada");
//                        Toast.makeText(view.getContext(), "Grabacion detenida", Toast.LENGTH_LONG).show();
//                        cambiarBotonEnviarMensaje(false);
//
//                        return true; //indicate we're done listening to this touch listener
//                    }
//
//                    case MotionEvent.ACTION_DOWN: {
//                        grabandoAudio = true;
//                        cambiarBotonEnviarMensaje(true);
//                        iniciarGrabacionAudio();
//                        Toast.makeText(view.getContext(), "Grabando...", Toast.LENGTH_LONG).show();
//                        ;
//                        Log.d(TAG, "Grabando audio...");
//
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

        /* Accion seleccionar archivo para enviar */
        btSubirArchivoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BT-ADJUNTAR ARCHIVO: ");

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, ADJUNTO_CHAT_REQUEST_CODE);

            }
        });

        /* Accion volver a la lista de chats */
        btVolverListaChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, ListarChatsActivity.class));
            }
        });

        /* Accion llamar al alumno del chat */
        btLlamarReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (originChatDataDto != null) {
                    Intent email = new Intent(Intent.ACTION_SEND);
                    //userInSession.getEmail()
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"kikocoya@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "HelpMe App - Un compañero necesita tu ayuda.");
                    email.putExtra(Intent.EXTRA_TEXT, "Hola, soy " + userInSession.getDisplayName() + "\n\n\nHelpMe App");

                    email.setType("text/plain");

                    startActivity(Intent.createChooser(email, "Enviando correo..."));
                }
            }
        });

        /* Accion sacar foto para enviar por chat */
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "CAMERA: ");

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_IMAGE_CHAT);
            }
        });


        if (originChatDataDto != null) {
            /* Mostrar img perfil y nombre del alumnoB */
            paintReceiverData();

            if (originChatDataDto.messages != null) {
                paintChatMessages();
            }
        }

        msgAdapter = new MensajeAdapter(chatMessages);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        recyclerConversacionChat.setLayoutManager(layoutManager);

        recyclerConversacionChat.setAdapter(msgAdapter);

        actualizarEstadoAlumnoReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Actualiza en tiempo real el estado del alumnoB (Otro usuario) del chat.
     */
    private void actualizarEstadoAlumnoReceiver() {
        dbReference.child(Alumno.REFERENCE)
                .child(originChatDataDto.receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, Object> resData = ((HashMap<String, Object>) snapshot.getValue());
                            String remoteStatus = resData.get(Alumno.STATUS).toString();

                            if (remoteStatus.equals(ONLINE_STATUS)) {
                                txStatusUsuarioReceiver.setText("online");

                            } else if (remoteStatus.equals(TYPING_STATUS)) {
                                txStatusUsuarioReceiver.setText("escribiendo...");

                            } else {
                                txStatusUsuarioReceiver.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "ACTUALIZAR ESTADO ALUMNO-B. " + error.getMessage());
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (grabandoAudio) {
            stopRecording();
        }
    }

    /**
     * Cambia la apariencia del botón de enviar mensaje.
     *
     * @param grabandoAudio true si el icono del botón es el micrófono (Iniciar grabación) y false si el icono es
     *                      el correspondiente a enviar mensaje (Detener grabacion).
     */
    private void cambiarBotonEnviarMensaje(final boolean grabandoAudio) {
        if (grabandoAudio) {
            btEnviarMensaje.setBackgroundResource(R.drawable.button_rounded_corners_red);
            btEnviarMensaje.setImageResource(R.drawable.ic_round_mic_24);

        } else {
            btEnviarMensaje.setBackgroundResource(R.drawable.button_rounded_corners_primary);
            btEnviarMensaje.setImageResource(R.drawable.ic_baseline_send_24);
        }
    }

    /**
     * Comprueba si el micrófono está activado.
     *
     * @return true si el micrófono del dispositivo está activado y false en caso contrario.
     */
    private boolean microfonoActivo() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void obtenerPermisoMicrofono() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_RECORD_AUDIO_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    /**
     * Obtiene el directorio de salida del audio grabado.
     *
     * @return
     */
    private String getAudioOutputFileName() {
//        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
//        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//        File file = new File(musicDirectory, "audiorecordtest" + ".mp3");

        return Environment.DIRECTORY_MUSIC + "/audio_test.3gp";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        if (microfonoActivo()) {
            obtenerPermisoMicrofono();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

        if (grabandoAudio) {
            stopRecording();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!ContentTypeUtils.validSize(data.getData())) {
            Toast.makeText(getApplicationContext(), "El archivo tiene que ser menor de 1MB", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Imagen tomada de la cámara */
        if (requestCode == CAMERA_IMAGE_CHAT && resultCode == Activity.RESULT_OK) {
            selectedImageToSend = (Bitmap) data.getExtras().get("data");
            Log.d(TAG, "Imagen recibida");

            ImageView selected = new ImageView(getApplicationContext());
            selected.setImageBitmap(selectedImageToSend);
            uploadImage(selected);

        } else if (requestCode == ADJUNTO_CHAT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            /* Documento o imagen seleccionado del dispositivo */
            Log.d(TAG, "Documento recibido...");
            Log.d(TAG, data.getData().toString());
            Uri selectedMediaUri = data.getData();
            Cursor cursor = getContentResolver().query(selectedMediaUri, null, null, null, null);
            int filenameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            cursor.moveToFirst();

            String filename = cursor.getString(filenameIndex);
//            String Fpath = selectedMediaUri.getPath();

            /* Subir el archivo seleccionado a Firebase */
            uploadFile(selectedMediaUri, filename);
        }
    }

    private void uploadImage(ImageView imageView) {
        if (originChatDataDto == null) {
            return;
        }

        ChatService.getInstance().uploadImage(imageView, originChatDataDto, new ChatService.MensajeCallback() {
            @Override
            public void callback() {
                Log.d(TAG, "Imagen subida al servidor!");
                msgAdapter.notifyDataSetChanged();
            }
        });
    }

    private void uploadFile(final Uri fileUri, final String filename) {
        if (originChatDataDto == null) {
            return;
        }

        ChatService.getInstance().uploadFile(fileUri, originChatDataDto, filename, new ChatService.MensajeCallback() {
            @Override
            public void callback() {
                Log.d(TAG, "Cambio recibido !");
                msgAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Procesa el contenido del intent origen.
     * El intent puede ser original de ListarChatsActivity o de ListadoAlumnosChatActivity
     *
     * @param bundle Bundle que contiene la información de origen.
     */
    private void processIntentExtras(Bundle bundle) {
        /* Si la procedencia del intent es del Activity listado de chats */
        if (bundle == null) {
            Log.e(TAG, "El bundle está vacío.");
            return;
        }

        originChatDataDto = (ChatSummaryDto) bundle.get(CHAT_SELECCIONADO);

    }

    /**
     * Muestra los datos del usuario receiver del chat en el activity.
     */
    private void paintReceiverData() {
        String name = originChatDataDto.receiverName;
        String imgUrl = originChatDataDto.receiverProfileImage;

        txNombreUsuarioReceiver.setText(name);
        Picasso.get().load(imgUrl).into(imgPerfilUsuarioReceiver);
    }

    /**
     * Muestra los mensajes del chat, actualizando el contenido dinámicamente en función del comportamiento
     * de la lista de mensajes de este (Si se añaden nuevos mensajes por ejemplo).
     * <p>
     * Los mensajes están contenidos dentro del recyclerView recyclerConversacionChat
     */
    private void paintChatMessages() {
        dbReference.child(Chat.REFERENCE)
                .child(originChatDataDto.chatId)
                .child(Mensaje.REFERENCE)
                .addValueEventListener(new ValueEventListener() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatMessages.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {

                                Map<String, Object> resData = ((HashMap<String, Object>) ds.getValue());

                                MensajeDto newMessage = new MensajeDto();

                                String content = resData.get(Mensaje.CONTENT).toString();
                                String createdAt = resData.get(Mensaje.CREATED_AT).toString();

                                newMessage.contenido = content;
                                newMessage.createdAt = createdAt;
                                newMessage.mimeType = resData.get(Mensaje.MESSAGE_TYPE).toString();
                                newMessage.userUid = resData.get(Mensaje.SENDER).toString();

                                Object status = resData.get(Mensaje.STATUS);

                                if (status != null) {
                                    MensajeStatus msgStatus = MensajeStatus.valueOf(status.toString());

                                    /* Si el mensaje no está leido, cambiar su estado a LEIDO */
                                    if (msgStatus.equals(MensajeStatus.RECIBIDO)
                                            || msgStatus.equals(MensajeStatus.ENVIADO)
                                            && resData.get(Mensaje.SENDER) == userInSession.getUid()) {

                                        Map<String, Object> statusPayload = new HashMap<>();
                                        statusPayload.put(Mensaje.STATUS, MensajeStatus.LEIDO);

                                        dbReference.child(Chat.REFERENCE)
                                                .child(originChatDataDto.chatId)
                                                .child(Mensaje.REFERENCE)
                                                .child(ds.getKey())
                                                .updateChildren(statusPayload).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        newMessage.status = MensajeStatus.LEIDO;
                                                    }
                                                });
                                    }

                                    newMessage.status = msgStatus;
                                } else {
                                    newMessage.status = MensajeStatus.LEIDO;
                                }

                                newMessage.status = resData.get(Mensaje.STATUS) != null
                                        ? MensajeStatus.valueOf(resData.get(Mensaje.STATUS).toString())
                                        : MensajeStatus.LEIDO;


                                if (resData.get(Mensaje.FILE_PRETTY_TYPE) != null) {
                                    newMessage.prettyType = resData.get(Mensaje.FILE_PRETTY_TYPE).toString();
                                }

                                if (resData.get(Mensaje.FILE_SIZE) != null) {
                                    newMessage.prettySize = resData.get(Mensaje.FILE_SIZE).toString();
                                }

                                if (resData.get(Mensaje.FILE_NAME) != null) {
                                    newMessage.filename = resData.get(Mensaje.FILE_NAME).toString();
                                }

                                chatMessages.add(newMessage);


                            }
                            msgAdapter.sortMessages();
                            msgAdapter.notifyDataSetChanged();
                            recyclerConversacionChat.smoothScrollToPosition(View.FOCUS_DOWN);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "PAIN_MESSAGES -- CANCELADO. " + error.getMessage());
                    }
                });
    }

    /**
     * Inicializa los campos del activity.
     */
    private void initFields() {
        btEnviarMensaje = (ImageButton) findViewById(R.id.button_enviar_mensaje_chat);
        txMensajeAEnviar = (EditText) findViewById(R.id.text_mensaje_enviar_chat);
        recyclerConversacionChat = (RecyclerView) findViewById(R.id.recycler_conversacion_chat);
        imgPerfilUsuarioReceiver = (ImageView) findViewById(R.id.img_receiver_user_chat);
        txNombreUsuarioReceiver = (TextView) findViewById(R.id.text_user_receiver_chat);
        txStatusUsuarioReceiver = (TextView) findViewById(R.id.text_user_receiver_status);
        btSubirArchivoChat = (ImageButton) findViewById(R.id.button_upload_file_chat);
        btVolverListaChats = (ImageButton) findViewById(R.id.button_back_to_chat_list);
        btLlamarReceiver = (ImageButton) findViewById(R.id.button_call_alumno_receiver);
        btCamera = (ImageButton) findViewById(R.id.button_camera_chat);

        /* Completar dinámicamente la imagen de perfil y el nombre del alumno receiver */
        txNombreUsuarioReceiver.setText(alumnoB.nombre);
        Picasso.get().load(alumnoB.urlFoto).into(imgPerfilUsuarioReceiver);


        txMensajeAEnviar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (status.equals(ONLINE_STATUS)) {
                    ChatService.getInstance().changeCurrentUserStatus(AlumnoStatus.ESCRIBIENDO.toString().toLowerCase(), new ChatService.AlumnoStatusCallback() {
                        @Override
                        public void callback() {
                            status = TYPING_STATUS;
                        }
                    });
                }
            }
        });

        txMensajeAEnviar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (status.equals(TYPING_STATUS) && txMensajeAEnviar.getText().length() > 0) {
                        ChatService.getInstance().changeCurrentUserStatus(AlumnoStatus.ONLINE.toString().toLowerCase(), new ChatService.AlumnoStatusCallback() {
                            @Override
                            public void callback() {
                                status = ONLINE_STATUS;
                            }
                        });

                    }
                }
            }
        });
    }

    // TODO: Completar
    private void iniciarGrabacionAudio() {
        if (!grabandoAudio) {
            startRecording();
        }
    }

    private void detenerGrabacionAudio() {
        if (grabandoAudio) {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audioOutputFileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        //recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
        recorder.setOutputFile(getAudioOutputFileName());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}