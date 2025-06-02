//版本：0.98
//version:0.98
//此代码作者是一个二次元宅！
//The code's author is a anime宅（anime enthusiast）!
//这是我的第一个J公开的AVA程序，有可以改进的方向请指出
//This is my first publicly released Java program, and I would like to know where I can make improvements.
/*你好，这是bilibili黑琼official制作的GUI框架？（存疑）（感觉可以当游戏引擎用）
这个文件旨在辅助新手理解与使用各种包（存疑），简化 Swing 的复杂性，提供开箱即用的网络与交互功能
适合需要快速开发 Java 桌面应用且希望减少底层代码的开发者，和JAVA初学者（还有我）使用
此文件还在开发中，请谨慎使用！*/
package PJG;//如果你有包，那么把这个包名称改为你的包名称，如果没有，就删掉此行

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.sound.sampled.*;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;//老长一堆import，可以看出来，我花了很多精力

public class PJGUI {
    // 文件操作工具类（视奸你的文件，BV号BV1qDUPYKEzf，方便我随取随用）
    public static final class FileUtils {
        private FileUtils() {} // 禁止实例化

        public static boolean saveText(String content, String filePath, boolean override) {
            File target = new File(filePath).getAbsoluteFile();
            try {
                // 路径安全检查（我要看看家里有没有冰红茶了）
                if (!isPathAllowed(target)) {
                    throw new SecurityException("禁止访问此路径");
                }

                if (target.exists() && !override) {
                    return false;
                }

                Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (IOException | SecurityException e) {
                ExceptionUtils.handleError("保存失败", e);
                return false;
            }
        }

        public static void saveBytes(byte[] data, String filePath, boolean override) throws IOException {
            File target = new File(filePath).getAbsoluteFile();
            if (!isPathAllowed(target)) {
                throw new SecurityException("路径越权访问");
            }

            if (target.exists() && !override) {
                throw new FileAlreadyExistsException(target.getPath());
            }

            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(data);
            }
        }

        public static boolean delete(String path) {
            File file = new File(path).getAbsoluteFile();
            if (!isPathAllowed(file)) {
                ExceptionUtils.handleError("删除失败", new SecurityException("无权操作此路径"));
                return false;
            }

            try {
                return Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                ExceptionUtils.handleError("删除失败", e);
                return false;
            }
        }

        public static String rename(String oldPath, String newName) {
            File src = new File(oldPath).getAbsoluteFile();
            if (!src.exists() || !isPathAllowed(src)) {
                return null;
            }

            File dest = new File(src.getParent(), sanitizeFilename(newName));
            try {
                return Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        .toAbsolutePath().toString();
            } catch (IOException e) {
                ExceptionUtils.handleError("重命名失败", e);
                return null;
            }
        }

        // 冰红茶运输路径白名单校验（示例：仅允许用户目录和当前工作目录）
        private static boolean isPathAllowed(File file) {
            try {
                String userHome = System.getProperty("user.home");
                String currentDir = System.getProperty("user.dir");
                String canonicalPath = file.getCanonicalPath();

                return canonicalPath.startsWith(userHome) ||
                        canonicalPath.startsWith(currentDir);
            } catch (IOException e) {
                return false;
            }
        }

        // 冰红茶消毒的处理
        private static String sanitizeFilename(String name) {
            return name.replaceAll("[\\\\/:*?\"<>|]", "_");
        }

        // 集成到Components类的保存按钮示例
        public static JButton createSaveButton(Consumer<File> onSave) {
            JFileChooser saver = new JFileChooser();
            saver.setDialogTitle("保存文件");
            saver.setAcceptAllFileFilterUsed(false);
            saver.addChoosableFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
            saver.addChoosableFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "png"));

            return Components.button("保存", () -> {
                if (saver.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selected = saver.getSelectedFile();
                    // 自动添加扩展名
                    String ext = ((FileNameExtensionFilter)saver.getFileFilter()).getExtensions()[0];
                    if (!selected.getName().contains(".")) {
                        selected = new File(selected.getParent(), selected.getName() + "." + ext);
                    }
                    onSave.accept(selected);
                }
            });
        }
    }

    // 冰红茶分配池的配置（放个miku占位）（我还放）
    private static final ThreadPoolExecutor NETWORK_POOL = new ThreadPoolExecutor(
            4, 16, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),//队列少了！不改！（浑琼乱入）
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger();//封闭的方法，合上了冰红茶和幻琼的脑壳

                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "net-pool-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            NETWORK_POOL.shutdown();
            try {
                if (!NETWORK_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                    NETWORK_POOL.shutdownNow().forEach(task ->
                            System.err.println("强制终止任务: " + task.toString()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("关闭过程被中断");
            }
        }));
    }

    // 应用的配置（放个miku占位）
    public static class AppConfig {
        public static String WS_ENDPOINT = "wss://echo.websocket.events";
        public static String HEALTH_CHECK_URL = "https://httpbin.org/get";
    }

    // （冰红茶外壳）GUI构建的系统
    public static WindowBuilder window(String title) {
        return new WindowBuilder(title);
    }

    public static final class WindowBuilder {
        private final JFrame frame;
        private final Container contentPane;
        private TrayIcon trayIcon;

        public WindowBuilder alwaysOnTop(boolean enable) {
            frame.setAlwaysOnTop(enable);
            return this;
        }

        // 新增窗口透明度的控制（今天的冰红茶也是非常好喝呢）
        public WindowBuilder setTransparency(float alpha) {
            frame.setUndecorated(true);
            frame.setBackground(new Color(0, 0, 0, alpha));
            return this;
        }

        // 新增拖拽的支持（放个miku占位）
        public WindowBuilder draggable() {
            AtomicReference<Point> mouseDownComp = new AtomicReference<>();
            frame.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mouseDownComp.set(e.getPoint());
                }
            });
            frame.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point currCoords = e.getLocationOnScreen();
                    frame.setLocation(
                            currCoords.x - mouseDownComp.get().x,
                            currCoords.y - mouseDownComp.get().y
                    );
                }
            });
            return this;
        }

        private WindowBuilder(String title) {
            frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            contentPane = frame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            frame.setSize(800, 600);
            centerWindow();
        }

        public WindowBuilder noBorder() {
            frame.setUndecorated(true);
            return this;
        }

        public WindowBuilder icon(String path) {
            try {
                Image image = ImageIO.read(new File(path));
                frame.setIconImage(image);
            } catch (IOException e) {
                ExceptionUtils.handleError("图标加载失败", e);
            }
            return this;
        }

        public WindowBuilder withTray(Image icon, List<MenuItem> menuItems) {
            if (SystemTray.isSupported()) {
                PopupMenu popup = new PopupMenu();
                if (menuItems != null) {
                    for (MenuItem item : menuItems) {
                        popup.add(item);
                    }
                }
                try {
                    trayIcon = new TrayIcon(icon, "App", popup);
                    SystemTray.getSystemTray().add(trayIcon);

                    // 窗口关闭时移除托盘图标(最正常的注释)
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            SystemTray.getSystemTray().remove(trayIcon);
                        }
                    });
                } catch (AWTException e) {
                    ExceptionUtils.handleError("系统托盘错误", e);
                }
            }
            return this;
        }

        public WindowBuilder add(Component comp) {
            contentPane.add(comp);
            return this;
        }

        public void show() {
            SwingUtilities.invokeLater(() -> {
                frame.pack();
                frame.setVisible(true);
            });
        }

        private void centerWindow() {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(
                    (screen.width - frame.getWidth()) / 2,
                    (screen.height - frame.getHeight()) / 2
            );
        }
    }

    // UI组件工厂（放个miku占位）
    public static final class Components {

        // 冰红茶主题系统
        private static Color themeBg = Color.WHITE;
        private static Color themeFg = Color.BLACK;
        private static Font themeFont = new Font("微软雅黑", Font.PLAIN, 12);

        private static final Set<Window> trackedWindows = Collections.newSetFromMap(
                new WeakHashMap<Window, Boolean>()
        );

        public static void applyTheme(Color bg, Color fg, Font font) {
            themeBg = bg;
            themeFg = fg;
            themeFont = font;
            UIManager.put("Panel.background", bg);
            UIManager.put("Button.background", bg);
            UIManager.put("Button.foreground", fg);
            UIManager.put("TextField.background", bg.brighter());
            UIManager.put("TextField.foreground", fg);
            UIManager.put("Label.foreground", fg);
            UIManager.put("Menu.background", bg);
            UIManager.put("Menu.foreground", fg);

            // 更新现有组件（AI要更新的，出事找miku，不要找幻琼，更不要找黑琼）
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            for (Window w : trackedWindows.toArray(new Window[0])) {
                if (w.isDisplayable()) {
                    SwingUtilities.updateComponentTreeUI(w);
                }
            }
        }

        // 冰红茶真的很好喝啊！
        public static JButton button(String text, Runnable action) {
            JButton btn = new JButton(text);
            btn.setFont(themeFont);
            btn.setForeground(themeFg);
            btn.addActionListener(e -> action.run());
            return btn;
        }

        public static JTextField textField(String hint, Consumer<String> onChange) {
            JTextField field = new JTextField(hint);
            Timer timer = new Timer(500, null);
            timer.setRepeats(false);

            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    trigger();
                }

                public void removeUpdate(DocumentEvent e) {
                    trigger();
                }

                public void changedUpdate(DocumentEvent e) {
                    trigger();
                }

                private void trigger() {
                    timer.stop();
                    if (timer.getActionListeners().length > 0) {
                        timer.removeActionListener(timer.getActionListeners()[0]);
                    }
                    timer.addActionListener(e -> onChange.accept(field.getText()));
                    timer.start();
                }
            });
            return field;
        }

        public static JLabel gifView(String path) {
            return new JLabel(new ImageIcon(path));
        }

        public static JComponent canvas(Consumer<Graphics2D> painter) {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    painter.accept((Graphics2D) g);
                }
            };
        }

        public static JLabel networkStatus() {
            JLabel label = new JLabel("● 离线");
            label.setForeground(Color.RED);
            new Timer(5000, e -> Net.checkConnectivity(status ->
                    label.setForeground(status ? Color.GREEN : Color.RED))).start();
            return label;
        }

        public static JButton fileChooser(Consumer<File> onFileSelected) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false); // 禁用"全部文件"选项
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                    "支持的术曲类型（文件类型）", "jpg", "png", "txt", "wav"));

            JButton button = new JButton("选择冰红茶（文件）");
            button.addActionListener(e -> {
                int result = fileChooser.showOpenDialog(button);
                if (result == JFileChooser.APPROVE_OPTION) {
                    onFileSelected.accept(fileChooser.getSelectedFile());
                }
            });
            return button;
        }

        public static JPopupMenu contextMenu(List<JMenuItem> items) {
            JPopupMenu menu = new JPopupMenu();
            if (items != null) { // 检查是否为null（冰红茶是否有毒）
                for (JMenuItem item : items) {
                    menu.add(item);
                }
            }
            return menu;
        }

        public static JComponent imageView(String path, Consumer<Graphics2D> painter) {
            return new SwingImagePanel(path, painter);
        }

        // 进度的指示器
        public static JComponent circularProgress(int size) {
            return new JComponent() {
                private int progress = 0;

                {
                    new Timer(50, e -> {
                        progress = (progress + 2) % 100;
                        repaint();
                    }).start();
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(themeBg.darker());
                    g2.setStroke(new BasicStroke(4));
                    g2.drawOval(2, 2, size - 4, size - 4);
                    g2.setColor(themeFg);
                    g2.drawArc(2, 2, size - 4, size - 4, 90, -(int) (3.6 * progress));
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(size, size);
                }
            };
        }
    }

    // 事件处理的系统
    public static final class Events {
        public static void onClick(Component comp, Runnable action) {
            if (comp instanceof AbstractButton) {
                ((AbstractButton) comp).addActionListener(e -> action.run());
            } else {
                comp.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {//普通的判断（不对！我Insert开着！（放个miku占位））
                            action.run();
                        }
                    }
                });
            }
        }

        public static void onKey(Component target, int keyCode, Runnable action) {
            target.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == keyCode) action.run();
                }
            });
            target.setFocusable(true);
            target.requestFocusInWindow();
        }

        public static void onDrop(JComponent target, Consumer<List<File>> handler) {
            target.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override//用的最多的（放个miku占位）
                @SuppressWarnings("unchecked")
                public boolean importData(TransferSupport support) {
                    try {
                        Transferable t = support.getTransferable();
                        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            handler.accept(files);
                            return true;
                        }
                    } catch (UnsupportedFlavorException | IOException e) {
                        ExceptionUtils.handleError("文件拖放错误", e);
                    }
                    return false;
                }
            });
        }

        public static void autoSubmit(JTextField field, String apiUrl) {
            field.addActionListener(e ->
                    Net.httpGet(apiUrl + "?q=" + encodeURI(field.getText()),
                            response -> JOptionPane.showMessageDialog(field, "响应: " + response),
                            error -> JOptionPane.showMessageDialog(field, "请求失败")
                    )
            );
        }

        private static String encodeURI(String text) {
            try {
                return URLEncoder.encode(text, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return text;
            }
        }
    }

    // 网络功能模块(远程运输冰红茶)(我知道这里有坑，但我不想填了，你们忍一下)
    public static final class Net {
        //（嗨嗨嗨，miku来了）
        public static class WebSocketClient {
            private Socket socket;
            private Consumer<String> onMessage;
            private Consumer<byte[]> onBinary;
            private volatile boolean running;

            public WebSocketClient(String url) {
                NETWORK_POOL.execute(() -> connect(url));
            }

            private void connect(String url) {
                try {
                    URI uri = new URI(url);
                    //AI，我谢谢你，帮我把这里的报错搞没了（放个miku占位）
                    if ("wss".equalsIgnoreCase(uri.getScheme())) {
                        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        socket = factory.createSocket(
                                uri.getHost(),
                                uri.getPort() > 0 ? uri.getPort() : 443
                        );
                        ((SSLSocket)socket).startHandshake(); // 新增SSL握手
                    } else {
                        socket = new Socket(
                                uri.getHost(),
                                uri.getPort() > 0 ? uri.getPort() : 80
                        );
                    }

                    byte[] keyBytes = new byte[16];
                    SecureRandom secureRandom = SecureRandom.getInstanceStrong(); // 增强随机性
                    secureRandom.nextBytes(keyBytes);
                    String key = Base64.getEncoder().encodeToString(keyBytes);

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                            socket.getOutputStream(), StandardCharsets.UTF_8), true); // 添加auto flush
                    writer.println("GET " + uri.getPath() + " HTTP/1.1");
                    writer.println("Host: " + uri.getHost());
                    writer.println("Upgrade: websocket");
                    writer.println("Connection: Upgrade");
                    writer.println("Sec-WebSocket-Key: " + key);
                    writer.println("Sec-WebSocket-Version: 13");
                    writer.println("Origin: " + uri.getScheme() + "://" + uri.getHost()); // 新增origin头
                    writer.println();
                    // 不要关闭writer（关掉术力口），保持流打开

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    String header;
                    boolean handshakeValid = false;
                    String serverAccept = null;

                    // 修复5：完整读取响应头
                    while ((header = reader.readLine()) != null) {
                        if (header.startsWith("HTTP/1.1 101")) {
                            handshakeValid = true;
                        }
                        if (header.startsWith("Sec-WebSocket-Accept:")) {
                            serverAccept = header.substring("Sec-WebSocket-Accept:".length()).trim();
                        }
                        if (header.isEmpty()) break;
                    }

                    // 修复6：双重验证握手
                    if (!handshakeValid || serverAccept == null) {
                        throw new IOException("握手失败: 无效状态码或缺少Sec-WebSocket-Accept");
                    }

                    // 修复7：正确计算期望值
                    String expected = Base64.getEncoder().encodeToString(
                            MessageDigest.getInstance("SHA-1").digest(
                                    (key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)
                            )
                    );
                    if (!serverAccept.equals(expected)) {
                        throw new IOException("密钥验证失败\n期望值：" + expected + "\n实际值：" + serverAccept);
                    }

                    running = true;
                    NETWORK_POOL.execute(this::listen);
                } catch (Exception e) {
                    // 修复8：增强错误处理
                    ExceptionUtils.handleError("连接失败 [" + url + "]", e);
                    closeQuietly(socket); // 确保关闭socket
                }
            }

            // 新增辅助方法 (类(冰红茶)内添加)
            private static void closeQuietly(Socket s) {
                try {
                    if (s != null && !s.isClosed()) {
                        s.close();
                    }
                } catch (IOException ignored) {}
            }

            private void listen() {
                try (InputStream in = socket.getInputStream()) {
                    while (running) {
                        // 读取帧头（放个miku占位）
                        byte[] header = new byte[2];
                        int bytesRead = in.read(header);
                        if (bytesRead != 2) {
                            if (bytesRead == -1) {
                                // 连接已关闭
                                running = false;
                                break;
                            }
                            // 部分读取，等待更多数据
                            continue;
                        }

                        boolean fin = (header[0] & 0x80) != 0;
                        int opcode = header[0] & 0x0F;
                        boolean masked = (header[1] & 0x80) != 0;
                        long payloadLength = header[1] & 0x7F;

                        // 处理扩展长度（冰红茶太多了也是不好的）
                        if (payloadLength == 126) {
                            byte[] ext = new byte[2];
                            if (in.read(ext) != 2) break;
                            payloadLength = ((ext[0] & 0xFF) << 8) | (ext[1] & 0xFF);
                        } else if (payloadLength == 127) {
                            byte[] ext = new byte[8];
                            if (in.read(ext) != 8) break;
                            payloadLength = 0;
                            for (int i = 0; i < 8; i++) {
                                payloadLength |= (long) (ext[i] & 0xFF) << (56 - 8 * i);
                            }
                        }

                        // 读取掩码（不知道说什么，放miku占位，表示这个是人写得）
                        byte[] mask = new byte[4];
                        if (masked) {
                            if (in.read(mask) != 4) break;
                        }

                        // 读取有效载荷
                        byte[] payload = new byte[(int) payloadLength];
                        int totalRead = 0;
                        while (totalRead < payload.length) {
                            int read = in.read(payload, totalRead, payload.length - totalRead);
                            if (read == -1) {
                                running = false;
                                break;
                            }
                            totalRead += read;
                        }

                        // 解掩码
                        if (masked) {
                            for (int i = 0; i < payload.length; i++) {
                                payload[i] ^= mask[i % 4];
                            }
                        }

                        // 处理不同帧类型
                        switch (opcode) {
                            case 0x01: // 文本帧
                                String text = new String(payload, StandardCharsets.UTF_8);
                                SwingUtilities.invokeLater(() -> {
                                    if (onMessage != null) onMessage.accept(text);
                                });
                                break;
                            case 0x02: // 二进制帧
                                byte[] finalPayload = payload;
                                SwingUtilities.invokeLater(() -> {
                                    if (onBinary != null) onBinary.accept(finalPayload);
                                });
                                break;
                            case 0x08: // 关闭帧
                                running = false;
                                break;
                            case 0x09: // Ping帧
                                sendPong(payload);
                                break;
                            case 0x0A: // Pong帧（miku）
                                // 忽略
                                break;
                        }
                    }
                } catch (IOException e) {
                    if (running) ExceptionUtils.handleError("WebSocket错误", e);
                } finally {
                    closeQuietly(socket);
                }
            }

            private void sendPong(byte[] payload) {
                NETWORK_POOL.execute(() -> {
                    try {
                        OutputStream out = socket.getOutputStream();
                        byte[] header = new byte[2];
                        header[0] = (byte) 0x8A; // FIN + Pong帧
                        header[1] = (byte) payload.length;

                        byte[] frame = new byte[header.length + payload.length];
                        System.arraycopy(header, 0, frame, 0, header.length);
                        System.arraycopy(payload, 0, frame, header.length, payload.length);
                        out.write(frame);
                        out.flush();
                    } catch (IOException e) {
                        ExceptionUtils.handleError("发送Pong失败", e);
                    }
                });
            }

            public void send(String message) {
                NETWORK_POOL.execute(() -> {
                    try {
                        OutputStream out = socket.getOutputStream();
                        byte[] data = message.getBytes(StandardCharsets.UTF_8);

                        // 构造帧头
                        byte[] header = new byte[2];
                        header[0] = (byte) 0x81; // FIN + 文本帧
                        if (data.length <= 125) {
                            header[1] = (byte) data.length;
                        } else if (data.length <= 65535) {
                            header[1] = (byte) 126;
                            byte[] lenBytes = new byte[2];
                            lenBytes[0] = (byte) ((data.length >> 8) & 0xFF);
                            lenBytes[1] = (byte) (data.length & 0xFF);
                            byte[] newHeader = new byte[4];
                            System.arraycopy(header, 0, newHeader, 0, 2);
                            System.arraycopy(lenBytes, 0, newHeader, 2, 2);
                            header = newHeader;
                        } else {
                            header[1] = (byte) 127;
                            byte[] lenBytes = new byte[8];
                            for (int i = 0; i < 8; i++) {
                                lenBytes[i] = (byte) ((data.length >> (56 - i * 8)) & 0xFF);
                            }
                            byte[] newHeader = new byte[10];
                            System.arraycopy(header, 0, newHeader, 0, 2);
                            System.arraycopy(lenBytes, 0, newHeader, 2, 8);
                            header = newHeader;
                        }

                        // 合并发送
                        byte[] frame;
                        if (header.length == 2) {
                            frame = new byte[header.length + data.length];
                            System.arraycopy(header, 0, frame, 0, header.length);
                            System.arraycopy(data, 0, frame, header.length, data.length);
                        } else {
                            frame = new byte[header.length + data.length];
                            System.arraycopy(header, 0, frame, 0, header.length);
                            System.arraycopy(data, 0, frame, header.length, data.length);
                        }
                        out.write(frame);
                        out.flush();
                    } catch (IOException e) {
                        ExceptionUtils.handleError("发送失败", e);
                    }
                });
            }

            public void close() {
                running = false;
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignored) {
                }
            }

            public void setOnMessage(Consumer<String> onMessage) {
                this.onMessage = onMessage;
            }

            public void setOnBinary(Consumer<byte[]> onBinary) {
                this.onBinary = onBinary;
            }
        }

        // HTTP GET请求（今天我生日，我许愿，我有喝不完的冰红茶，听不玩的术，花不完的钱！）
        public static void httpGet(String url, Consumer<String> success, Consumer<Exception> fail) {
            NETWORK_POOL.execute(() -> {
                int retry = 0;
                Exception lastException = null;

                while (retry < 3) {
                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection) new URL(url).openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(10000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "PJGUI/0.94");

                        int responseCode = conn.getResponseCode();
                        if (responseCode >= 400) {
                            throw new IOException("HTTP错误: " + responseCode);
                        }

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            SwingUtilities.invokeLater(() -> success.accept(response.toString()));
                            return; // 成功则退出
                        }
                    } catch (SocketTimeoutException e) {
                        lastException = e;
                        System.out.println("请求超时，重试第" + (retry + 1) + "次");
                    } catch (Exception e) {
                        lastException = e;
                        break; // 非超时错误直接退出
                    } finally {
                        if (conn != null) conn.disconnect();
                    }

                    // 指数退避等待
                    try {
                        Thread.sleep((long) (1000 * Math.pow(2, retry)));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    retry++;
                }

                if (lastException != null) {
                    Exception finalLastException = lastException;
                    SwingUtilities.invokeLater(() -> fail.accept(finalLastException));
                }
            });
        }

        // 网络状态检查
        public static void checkConnectivity(Consumer<Boolean> callback) {
            NETWORK_POOL.execute(() -> {
                try {
                    URL url = new URL(AppConfig.HEALTH_CHECK_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    int code = conn.getResponseCode();
                    SwingUtilities.invokeLater(() -> callback.accept(code == 200));
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> callback.accept(false));
                }
            });
        }
    }

    // 音频模块（初音未来！）
    public static class AudioPlayer {
        private SourceDataLine audioLine;
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);
        private final AudioFormat format;
        private final byte[] audioData;
        private float volume = 1.0f;
        private Thread playThread;
        private float speed = 1.0f;
        private float pitch = 1.0f;
        private List<AudioEffect> effects = new ArrayList<>();
        // 音频播放功能（好耶！可以播放术力口了！）
        public static void playAudio(byte[] pcmData) {
            NETWORK_POOL.execute(() -> {
                try {
                    AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                    SourceDataLine line = AudioSystem.getSourceDataLine(format);
                    line.open(format);
                    line.start();
                    line.write(pcmData, 0, pcmData.length);
                    line.drain();
                    line.close();
                } catch (LineUnavailableException e) {
                    ExceptionUtils.handleError("音频播放失败", e);
                }
            });
        }

        public static void playAudioFile(String path) {
            NETWORK_POOL.execute(() -> {
                try {
                    AudioPlayer player = new AudioPlayer(new File(path));
                    player.play();
                } catch (Exception e) {
                    ExceptionUtils.handleError("播放失败", e);
                }
            });
        }

        public AudioPlayer(File audioFile) throws UnsupportedAudioFileException, IOException {
            this(AudioSystem.getAudioInputStream(audioFile));
        }

        public AudioPlayer(InputStream stream) throws UnsupportedAudioFileException, IOException {
            this(AudioSystem.getAudioInputStream(stream));
        }

        private AudioPlayer(AudioInputStream ais) throws UnsupportedAudioFileException, IOException {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    ais.getFormat().getSampleRate(),
                    16, // 统一转换为16位PCM
                    ais.getFormat().getChannels(),
                    ais.getFormat().getChannels()*2,
                    ais.getFormat().getSampleRate(),
                    false
            );
            this.format = targetFormat;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = ais.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                this.audioData = baos.toByteArray();
            }
        }

        // 添加回声效果
        public void addEchoEffect(int delayMs, float decay) {
            byte[] original = audioData.clone();
            int delayBytes = (int)(delayMs * format.getSampleRate()/1000) * format.getFrameSize();

            for(int i = delayBytes; i < audioData.length; i++){
                audioData[i] = (byte)(original[i] + decay * original[i - delayBytes]);
            }
        }

        // 添加音效处理接口（你说，以后这个东西成了音乐软件会发生什么，搞一个术力口？）
        public interface AudioEffect {
            byte[] process(byte[] input, AudioFormat format);
        }

        // 回声效果实现
        public static class EchoEffect implements AudioEffect {
            private final int delayMs;
            private final float decay;

            public EchoEffect(int delayMs, float decay) {
                this.delayMs = delayMs;
                this.decay = decay;
            }

            public byte[] process(byte[] input, AudioFormat format) {
                byte[] output = Arrays.copyOf(input, input.length);
                int delayBytes = (int)(delayMs * format.getSampleRate()/1000) * format.getFrameSize();

                for(int i=delayBytes; i<input.length; i++){
                    output[i] += decay * input[i - delayBytes];
                }
                return output;
            }
        }/*匹：终于发了一首曲子了，应该能狙过他吧
            匹：(打开手机)
            匹：Deco*27真的有27个人吗？（取自高赞评论）*/

        // 在AudioPlayer中添加效果链（不知道说什么，放miku占位，表示这个是人写得）
        public void addEffect(AudioEffect effect) {
            effects.add(effect);
        }

        public synchronized void play() {
            if (isPlaying.get()) return;

            isPlaying.set(true);
            playThread = new Thread(() -> {
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {//我取搞点冰红茶喝
                    line.open(format);
                    line.start();

                    // 音量控制（好的！搞到了）
                    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                        float range = volumeControl.getMaximum() - volumeControl.getMinimum();
                        volumeControl.setValue(volumeControl.getMinimum() + range * volume);
                    }

                    int chunkSize = (int)(line.getBufferSize() * speed);
                    int position = 0;

                    while (isPlaying.get() && position < audioData.length) {
                        int end = Math.min(position + chunkSize, audioData.length);
                        byte[] chunk = Arrays.copyOfRange(audioData, position, end);

                        // 应用效果链
                        for (AudioEffect effect : effects) {
                            chunk = effect.process(chunk, format);
                        }

                        line.write(chunk, 0, chunk.length);
                        position += chunkSize;
                    }

                    line.drain();
                } catch (LineUnavailableException e) {
                    ExceptionUtils.handleError("音频设备不可用", e);
                } finally {
                    isPlaying.set(false);
                }
            }, "AudioPlayer-Thread");
            playThread.start();
        }

        public synchronized void stop() {
            isPlaying.set(false);
            if (playThread != null) {
                playThread.interrupt();
                try {
                    playThread.join(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (audioLine != null) {
                audioLine.stop();
                audioLine.close();
            }
        }

        public void setVolume(float level) {
            this.volume = Math.max(0f, Math.min(1f, level));
        }

        public void setSpeed(float speed) {
            this.speed = Math.max(0.5f, Math.min(2.0f, speed));
        }

        public void setPitch(float pitch) {
            this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        }

        public boolean isPlaying() {
            return isPlaying.get();
        }

        public byte[] getAudioData() {
            return audioData.clone();
        }

        public AudioFormat getFormat() {
            return format;
        }
    }

    // 音频界面组件（术....术...术！不对，略nd，你怎么在这里）略nd:百万啦，时间：2025-02-25 15:17，点赞量：521（看来是热乎的）
    public static final class AudioComponents {
        public static JPanel createController(AudioPlayer player) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JButton playButton = new JButton("▶");
            playButton.addActionListener(e -> {
                if (player.isPlaying()) {
                    player.stop();
                    playButton.setText("▶");
                } else {
                    player.play();
                    playButton.setText("⏸");
                }
            });

            JSlider volumeSlider = new JSlider(0, 100, 50);
            volumeSlider.setPreferredSize(new Dimension(100, 20));
            volumeSlider.addChangeListener(e ->
                    player.setVolume(volumeSlider.getValue() / 100f)
            );

            JProgressBar progressBar = new JProgressBar();
            progressBar.setStringPainted(true);

            panel.add(playButton, BorderLayout.WEST);
            panel.add(progressBar, BorderLayout.CENTER);
            panel.add(volumeSlider, BorderLayout.EAST);

            new Timer(100, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (player.isPlaying()) {
                        progressBar.setString("播放中...");
                    } else {
                        progressBar.setString("已停止");
                    }
                }
            }).start();

            // 变速滑块（纯情？那是什么？爱情？又是什么？~~~）
            JSlider speedSlider = new JSlider(50, 200, 100);
            speedSlider.setPaintLabels(true);
            speedSlider.addChangeListener(e ->
                    player.setSpeed(speedSlider.getValue()/100f)
            );

            // 音效选择（搞一个术曲编辑器怎么样啊🤓🤓🤓，你觉得怎么样，牢黑）
            JComboBox<AudioPlayer.AudioEffect> effectSelect = new JComboBox<>();
            effectSelect.addItem(new AudioPlayer.EchoEffect(300, 0.5f));
            effectSelect.addItem(null); // 无效果
            effectSelect.addActionListener(e -> {
                AudioPlayer.AudioEffect selected = (AudioPlayer.AudioEffect) effectSelect.getSelectedItem();
                if (selected != null) {
                    player.addEffect(selected);
                }
            });//OK的

            panel.add(new JLabel("速度:"), BorderLayout.NORTH);
            panel.add(speedSlider, BorderLayout.CENTER);
            panel.add(effectSelect, BorderLayout.SOUTH);

            return panel;
        }

        public static JComponent createWaveform(byte[] pcmData, AudioFormat format) {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    g2d.setColor(Color.BLUE);
                    int sampleSize = format.getSampleSizeInBits() / 8;
                    int channels = format.getChannels();
                    int totalSamples = pcmData.length / (sampleSize * channels);
                    int step = Math.max(1, totalSamples / getWidth());

                    for (int x = 0; x < getWidth(); x++) {
                        int sampleIndex = (int) ((double) x / getWidth() * totalSamples);
                        int bytePos = sampleIndex * sampleSize * channels;

                        if (bytePos + sampleSize > pcmData.length) break;

                        short sample = (short) ((pcmData[bytePos + 1] << 8) | pcmData[bytePos]);
                        int y = (int) ((sample / 32768.0) * (getHeight() / 2)) + getHeight() / 2;
                        g2d.drawLine(x, getHeight() / 2, x, y);
                    }
                    g2d.dispose();
                }
            };
        }

        // 音量波形可视化（基于现有PCM数据）
        public static JComponent createVolumeMeter(AudioPlayer player) {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    byte[] data = player.getAudioData();
                    int width = getWidth();
                    int height = getHeight();

                    // 简单波形绘制(放miku占位)
                    for(int x=0; x<width; x++) {
                        int sampleIndex = x * data.length / width;
                        int value = Math.abs(data[sampleIndex]);
                        g.drawLine(x, height/2 - value, x, height/2 + value);
                    }
                }
            };
        }
    }

    // 异常处理工具（冰红茶又没了,TwT）
    public static class ExceptionUtils {
        public static void handleError(String message, Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    message + ": " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 图像面板实现(放miku占位)
    private static class SwingImagePanel extends JPanel {
        private final String imagePath;
        private final Consumer<Graphics2D> painter;
        private List<VideoFilter> filters = new ArrayList<>();

        SwingImagePanel(String path, Consumer<Graphics2D> painter) {
            this.imagePath = path;
            this.painter = painter;
            setPreferredSize(new Dimension(400, 300));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                if (image != null) { // 检查图像是否加载成功
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);

                    // 应用所有滤镜
                    for(VideoFilter filter : filters){
                        image = filter.process(image);
                    }

                    painter.accept(g2d);
                    g2d.dispose();
                }
            } catch (IOException e) {
                ExceptionUtils.handleError("图片加载失败", e);
            }
        }

        public void addFilter(VideoFilter filter) {
            filters.add(filter);
        }
    }

    // 调试工具(放miku占位，证明是人写得)
    public static class Debug {
        public static void toggleBorderDebug() {
            // 切换边框调试状态
            if (UIManager.get("Component.border") == null) {
                UIManager.put("Component.border", BorderFactory.createLineBorder(Color.RED));
            } else {
                UIManager.put("Component.border", null);
            }

            // 更新所有已打开的窗口的UI
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame instanceof JFrame) {
                    SwingUtilities.updateComponentTreeUI((JFrame) frame);
                }
            }
        }
    }


    public static JComponent videoFilterPanel(SwingImagePanel videoPanel) {//(放miku占位，证明是人写得)
        JPanel panel = new JPanel(new GridLayout(0, 3));
        panel.setBorder(BorderFactory.createTitledBorder("视频滤镜控制"));

        // 基础颜色控制
        JSlider redSlider = createColorSlider("红", 1f, f ->//teto的发色
                videoPanel.addFilter(new ColorFilter(f, 1, 1))
        );
        JSlider greenSlider = createColorSlider("绿", 1f, f ->//miku的发色
                videoPanel.addFilter(new ColorFilter(1, f, 1))
        );
        JSlider blueSlider = createColorSlider("蓝", 1f, f ->//也是miku的发色(越来越蓝了（wei）)
                videoPanel.addFilter(new ColorFilter(1, 1, f))
        );
        //look my eyes! tell me___baby____()A.what B.who C.why

        // HSV色彩空间控制
        JSlider hueSlider = createColorSlider("色相", 0f, f ->
                videoPanel.addFilter(new HSVColorFilter(f, 1, 1))
        );
        JSlider saturationSlider = createColorSlider("饱和度", 1f, f ->
                videoPanel.addFilter(new HSVColorFilter(0, f, 1))
        );
        JSlider valueSlider = createColorSlider("明度", 1f, f ->
                videoPanel.addFilter(new HSVColorFilter(0, 1, f))
        );

        // 特效的控制
        JSlider rippleSlider = new JSlider(0, 100, 0);
        rippleSlider.addChangeListener(e ->
                videoPanel.addFilter(new RippleEffect(
                        rippleSlider.getValue() / 10.0,
                        0.02
                ))
        );

        JSlider blurSlider = new JSlider(0, 100, 0);
        blurSlider.addChangeListener(e ->
                videoPanel.addFilter(new BlurFilter(blurSlider.getValue() / 10.0f))
        );

        // 组合滤镜的控制
        JButton addCompositeBtn = new JButton("添加组合滤镜");
        addCompositeBtn.addActionListener(e -> {
            CompositeFilter composite = new CompositeFilter();
            composite.addFilter(new ColorFilter(1.2f, 1.0f, 0.8f));
            composite.addFilter(new RippleEffect(3.0, 0.03));
            videoPanel.addFilter(composite);
            JOptionPane.showMessageDialog(panel, "已添加预设组合滤镜");
        });

        // 性能的监控
        JCheckBox profileCheck = new JCheckBox("启用性能监控");
        profileCheck.addActionListener(e -> {
            if (profileCheck.isSelected()) {
                videoPanel.addFilter(new ProfilingFilter("滤镜处理"));
            }
        });

        // 区域的选择
        JButton regionBtn = new JButton("添加区域滤镜");
        regionBtn.addActionListener(e -> {
            Rectangle region = new Rectangle(100, 100, 200, 150);
            RegionFilter regionFilter = new RegionFilter(
                    new BlurFilter(2.0f),
                    region
            );
            videoPanel.addFilter(regionFilter);
            JOptionPane.showMessageDialog(panel, "已添加区域模糊滤镜");
        });

        // 动态的效果（what can i say? manba!）
        JButton pulseBtn = new JButton("添加脉动效果");
        pulseBtn.addActionListener(e -> {
            videoPanel.addFilter(new PulsatingRipple());
            JOptionPane.showMessageDialog(panel, "已添加脉动波纹效果");
        });

        // 布局组件(小绵羊magens写的歌不错啊，关注了)
        panel.add(new JLabel("RGB控制:"));
        panel.add(redSlider);
        panel.add(greenSlider);
        panel.add(blueSlider);

        panel.add(new JLabel("HSV控制:"));
        panel.add(hueSlider);
        panel.add(saturationSlider);
        panel.add(valueSlider);

        panel.add(new JLabel("特效:"));
        panel.add(rippleSlider);
        panel.add(blurSlider);

        panel.add(new JLabel("高级功能:"));
        panel.add(addCompositeBtn);
        panel.add(profileCheck);
        panel.add(regionBtn);
        panel.add(pulseBtn);

        return panel;
    }

    private static JSlider createColorSlider(String label, float init, Consumer<Float> action) {
        JSlider slider = new JSlider(0, 200, (int)(init * 100));
        slider.setBorder(BorderFactory.createTitledBorder(label));
        slider.setPaintLabels(true);
        slider.addChangeListener(e ->
                action.accept(slider.getValue() / 100f)
        );
        return slider;
    }

    // 视频处理接口，冰红茶很好喝，你要不要尝尝
    public interface VideoFilter {
        BufferedImage process(BufferedImage frame);
    }

    // 色调调整滤镜（和miku的头发一样）（后面让AI优化了一下，出事找AI优化，不要找黑琼噢）
    // 增强版色调调整滤镜（支持多种颜色空间和效果）
    public static class ColorFilter implements VideoFilter {
        // 颜色调整模式
        public enum ColorMode {
            RGB, HSV, HSL, GRAYSCALE, SEPIA, INVERT
        }

        private ColorMode mode = ColorMode.RGB;
        private float r, g, b;          // RGB调整参数
        private float hueShift;         // 色相偏移 (0-1)
        private float saturationFactor; // 饱和度乘数
        private float lightnessFactor;  // 明度乘数
        private boolean useFastRendering = true; // 性能优化开关

        // RGB模式构造器
        public ColorFilter(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }


        // 预设效果构造器
        public static ColorFilter createPreset(ColorMode preset) {
            ColorFilter filter = new ColorFilter(1, 1, 1);
            filter.mode = preset;
            return filter;
        }

        // 设置渲染模式（性能/质量）
        public void setFastRendering(boolean fast) {
            this.useFastRendering = fast;
        }

        @Override
        public BufferedImage process(BufferedImage src) {
            if (useFastRendering) {
                return processFast(src);
            } else {
                return processHighQuality(src);
            }
        }

        // 高性能处理（使用位图操作）
        private BufferedImage processFast(BufferedImage src) {
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage dst = new BufferedImage(width, height, src.getType());
            int[] pixels = new int[width * height];

            // 批量获取像素
            src.getRGB(0, 0, width, height, pixels, 0, width);

            // 并行处理像素
            IntStream.range(0, pixels.length).parallel().forEach(i -> {
                int argb = pixels[i];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // 应用颜色变换
                int[] newColor = transformColor(r, g, b);

                // 重新打包ARGB
                pixels[i] = (a << 24) | (newColor[0] << 16) | (newColor[1] << 8) | newColor[2];
            });

            // 设置处理后的像素
            dst.setRGB(0, 0, width, height, pixels, 0, width);
            return dst;
        }

        // 高质量处理（逐个像素）
        private BufferedImage processHighQuality(BufferedImage src) {
            BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            for (int y = 0; y < src.getHeight(); y++) {
                for (int x = 0; x < src.getWidth(); x++) {
                    Color c = new Color(src.getRGB(x, y), true);
                    int[] newColor = transformColor(c.getRed(), c.getGreen(), c.getBlue());
                    dst.setRGB(x, y, new Color(
                            newColor[0], newColor[1], newColor[2], c.getAlpha()
                    ).getRGB());
                }
            }
            return dst;
        }

        // 核心颜色变换逻辑
        private int[] transformColor(int r, int g, int b) {
            switch (mode) {
                case RGB:
                    return applyRgbTransform(r, g, b);
                case HSV:
                    return applyHsvTransform(r, g, b);
                case HSL:
                    return applyHslTransform(r, g, b);
                case GRAYSCALE:
                    return applyGrayscale(r, g, b);
                case SEPIA:
                    return applySepia(r, g, b);
                case INVERT:
                    return applyInvert(r, g, b);
                default:
                    return new int[]{r, g, b};
            }
        }

        // RGB变换
        private int[] applyRgbTransform(int r, int g, int b) {
            int newR = clamp((int) (r * this.r));
            int newG = clamp((int) (g * this.g));
            int newB = clamp((int) (b * this.b));
            return new int[]{newR, newG, newB};
        }

        // HSV变换（色相/饱和度/明度）
        private int[] applyHsvTransform(int r, int g, int b) {
            float[] hsv = Color.RGBtoHSB(r, g, b, null);

            // 调整色相
            hsv[0] = (hsv[0] + hueShift) % 1.0f;
            if (hsv[0] < 0) hsv[0] += 1.0f;

            // 调整饱和度
            hsv[1] = Math.min(1.0f, hsv[1] * saturationFactor);

            // 调整明度
            hsv[2] = Math.min(1.0f, hsv[2] * lightnessFactor);

            int rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
            return new int[]{
                    (rgb >> 16) & 0xFF,
                    (rgb >> 8) & 0xFF,
                    rgb & 0xFF
            };
        }

        // HSL的变换（更自然的明度处理）
        private int[] applyHslTransform(int r, int g, int b) {
            // 转换RGB到HSL
            float[] hsl = rgbToHsl(r, g, b);

            // 调整的色相（miku）
            hsl[0] = (hsl[0] + hueShift) % 360f;
            if (hsl[0] < 0) hsl[0] += 360f;

            // 调整的饱和度
            hsl[1] = Math.min(100f, hsl[1] * saturationFactor);

            // 调整的明度
            hsl[2] = Math.min(100f, hsl[2] * lightnessFactor);

            // 转换回RGB
            return hslToRgb(hsl[0], hsl[1], hsl[2]);
        }

        // 灰度的效果
        private int[] applyGrayscale(int r, int g, int b) {
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            return new int[]{gray, gray, gray};
        }

        // 复古棕褐色的效果
        private int[] applySepia(int r, int g, int b) {
            int newR = clamp((int) (0.393 * r + 0.769 * g + 0.189 * b));
            int newG = clamp((int) (0.349 * r + 0.686 * g + 0.168 * b));
            int newB = clamp((int) (0.272 * r + 0.534 * g + 0.131 * b));
            return new int[]{newR, newG, newB};
        }

        // 颜色的反转
        private int[] applyInvert(int r, int g, int b) {
            return new int[]{255 - r, 255 - g, 255 - b};
        }

        // RGB转HSL辅助的方法
        private float[] rgbToHsl(int r, int g, int b) {
            float rf = r / 255f;
            float gf = g / 255f;
            float bf = b / 255f;

            float max = Math.max(Math.max(rf, gf), bf);
            float min = Math.min(Math.min(rf, gf), bf);
            float delta = max - min;

            float h = 0, s, l = (max + min) / 2;

            if (delta != 0) {
                s = delta / (1 - Math.abs(2 * l - 1));

                if (max == rf) {
                    h = 60 * (((gf - bf) / delta) % 6);
                } else if (max == gf) {
                    h = 60 * (((bf - rf) / delta) + 2);
                } else {
                    h = 60 * (((rf - gf) / delta) + 4);
                }
            } else {
                s = 0;
            }

            if (h < 0) h += 360;
            return new float[]{h, s * 100, l * 100};
        }

        // HSL转RGB辅助方法
        private int[] hslToRgb(float h, float s, float l) {
            s /= 100;
            l /= 100;

            float c = (1 - Math.abs(2 * l - 1)) * s;
            float x = c * (1 - Math.abs((h / 60) % 2 - 1));
            float m = l - c / 2;

            float r, g, b;

            if (h < 60) {
                r = c; g = x; b = 0;
            } else if (h < 120) {
                r = x; g = c; b = 0;
            } else if (h < 180) {
                r = 0; g = c; b = x;
            } else if (h < 240) {
                r = 0; g = x; b = c;
            } else if (h < 300) {
                r = x; g = 0; b = c;
            } else {
                r = c; g = 0; b = x;
            }

            return new int[]{
                    clamp((int) ((r + m) * 255)),
                    clamp((int) ((g + m) * 255)),
                    clamp((int) ((b + m) * 255))
            };
        }

        // 确保颜色值在0-255范围内
        private int clamp(int value) {
            return Math.max(0, Math.min(255, value));
        }
    }


    // 波纹扭曲效果（喝冰红茶）
    public static class RippleEffect implements VideoFilter {
        // 波纹参数
        private double amplitude;//黄灯不慌
        private double frequency;//放个miku
        private double centerX = 0.5;  // 波纹中心X (0.0-1.0)
        private double centerY = 0.5;  // 波纹中心Y (0.0-1.0)
        private double timeFactor = 0.0; // 时间因子用于动态效果
        private boolean interpolate = true; // 是否使用插值采样

        // 性能的优化
        private final ExecutorService executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        // 构造的方法
        public RippleEffect(double amplitude, double frequency) {
            this.amplitude = amplitude;
            this.frequency = frequency;
        }

        // 带中心点的构造方法
        public RippleEffect(double amplitude, double frequency, double centerX, double centerY) {
            this(amplitude, frequency);
            this.centerX = centerX;
            this.centerY = centerY;
        }

        // 设置时间的因子（用于动态效果）
        public void setTimeFactor(double timeFactor) {
            this.timeFactor = timeFactor;
        }

        // 设置插值的采样（magens的非常exit好听啊）
        public void setInterpolate(boolean interpolate) {
            this.interpolate = interpolate;
        }

        @Override
        public BufferedImage process(BufferedImage src) {
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage dst = new BufferedImage(width, height, src.getType());

            // 计算实际中心点的坐标
            int centerPixelX = (int)(centerX * width);
            int centerPixelY = (int)(centerY * height);

            // 使用多线程的处理(最正常的注释)
            List<Future<?>> futures = new ArrayList<>();
            int processors = Runtime.getRuntime().availableProcessors();
            int segmentHeight = height / processors;

            for (int i = 0; i < processors; i++) {
                final int startY = i * segmentHeight;
                final int endY = (i == processors - 1) ? height : (i + 1) * segmentHeight;

                futures.add(executor.submit(() -> {
                    processSegment(src, dst, width, centerPixelX, centerPixelY, startY, endY);
                }));
            }

            // 等待所有线程的完成
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    ExceptionUtils.handleError("波纹处理错误", e);
                }
            }

            return dst;
        }

        private void processSegment(BufferedImage src, BufferedImage dst,
                                    int width, int centerX, int centerY,
                                    int startY, int endY) {
            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < width; x++) {
                    // 计算到中心的距离
                    double dx = x - centerX;
                    double dy = y - centerY;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    // 计算波纹的偏移（加入时间因子）
                    double offsetX = amplitude * Math.sin(distance * frequency + timeFactor);
                    double offsetY = amplitude * Math.cos(distance * frequency + timeFactor);

                    double srcX = x + offsetX;
                    double srcY = y + offsetY;

                    // 边界的检查
                    srcX = Math.max(0, Math.min(width - 1, srcX));//好消息，deco更新了，坏消息，重置之前的老歌，且GUMI改初音，这歌比我大
                    srcY = Math.max(0, Math.min(src.getHeight() - 1, srcY));

                    if (interpolate) {
                        // 双线性插值的采样(匹老板怎么还没有更新)
                        dst.setRGB(x, y, bilinearInterpolate(src, srcX, srcY));
                    } else {
                        // 最近邻的采样
                        int sx = (int) Math.round(srcX);
                        int sy = (int) Math.round(srcY);
                        dst.setRGB(x, y, src.getRGB(sx, sy));
                    }
                }
            }
        }

        // 双线性插值的实现
        private int bilinearInterpolate(BufferedImage image, double x, double y) {
            int x1 = (int) Math.floor(x);
            int y1 = (int) Math.floor(y);
            int x2 = Math.min(image.getWidth() - 1, x1 + 1);
            int y2 = Math.min(image.getHeight() - 1, y1 + 1);

            double xRatio = x - x1;
            double yRatio = y - y1;
            double xOpposite = 1.0 - xRatio;
            double yOpposite = 1.0 - yRatio;

            int rgb1 = image.getRGB(x1, y1);
            int rgb2 = image.getRGB(x2, y1);
            int rgb3 = image.getRGB(x1, y2);
            int rgb4 = image.getRGB(x2, y2);

            // 插值的计算（放个...冰红茶？）
            int r = (int) (
                    ((rgb1 >> 16) & 0xFF) * xOpposite * yOpposite +
                            ((rgb2 >> 16) & 0xFF) * xRatio * yOpposite +
                            ((rgb3 >> 16) & 0xFF) * xOpposite * yRatio +
                            ((rgb4 >> 16) & 0xFF) * xRatio * yRatio
            );

            int g = (int) (
                    ((rgb1 >> 8) & 0xFF) * xOpposite * yOpposite +
                            ((rgb2 >> 8) & 0xFF) * xRatio * yOpposite +
                            ((rgb3 >> 8) & 0xFF) * xOpposite * yRatio +
                            ((rgb4 >> 8) & 0xFF) * xRatio * yRatio
            );

            int b = (int) (
                    (rgb1 & 0xFF) * xOpposite * yOpposite +
                            (rgb2 & 0xFF) * xRatio * yOpposite +
                            (rgb3 & 0xFF) * xOpposite * yRatio +
                            (rgb4 & 0xFF) * xRatio * yRatio
            );

            return (r << 16) | (g << 8) | b;
        }

        // 关闭线程池（放miku）我喜欢写注释怎么着你了
        public void dispose() {
            executor.shutdown();
        }
    }

    // 动画控制器（放miku）
    public static class Animator {
        private static final Timer animTimer = new Timer(16, null);
        private static final Map<Object, AnimationTask> activeAnimations = new ConcurrentHashMap<>();

        static {
            animTimer.addActionListener(e -> updateAnimations());
            animTimer.start();
        }

        // 动画类型枚举（放miku）
        public enum AnimationType {
            FADE_IN, FADE_OUT, SLIDE_IN, SLIDE_OUT, SCALE, ROTATE, CUSTOM
        }

        // 缓动函数枚举（放miku）
        public enum Easing {
            LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, BOUNCE, ELASTIC
        }

        // 动画任务类（放miku）
        private static class AnimationTask {
            final Object target;
            final String property;
            final float start;
            final float end;
            final long duration;
            final long startTime;
            final Easing easing;
            final Runnable onComplete;
            final AnimationType type;
            final boolean removeOnComplete;

            AnimationTask(Object target, String property, float start, float end,
                          long duration, Easing easing, Runnable onComplete,
                          AnimationType type, boolean removeOnComplete) {
                this.target = target;
                this.property = property;
                this.start = start;
                this.end = end;
                this.duration = duration;
                this.startTime = System.currentTimeMillis();
                this.easing = easing;
                this.onComplete = onComplete;
                this.type = type;
                this.removeOnComplete = removeOnComplete;
            }
        }

        // 主更新循环（放miku）
        private static void updateAnimations() {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<Object, AnimationTask>> it = activeAnimations.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<Object, AnimationTask> entry = it.next();
                AnimationTask task = entry.getValue();
                float progress = Math.min(1.0f, (currentTime - task.startTime) / (float) task.duration);

                // 应用缓动函数（放miku）
                float easedProgress = applyEasing(progress, task.easing);
                float value = task.start + (task.end - task.start) * easedProgress;

                // 应用值到目标属性（放miku）
                applyValue(task.target, task.property, value);

                // 检查动画是否完成（放miku）
                if (progress >= 1.0f) {
                    if (task.onComplete != null) {
                        task.onComplete.run();
                    }
                    if (task.removeOnComplete) {
                        it.remove();
                    }
                }
            }
        }

        // 应用缓动函数（放miku）
        private static float applyEasing(float t, Easing easing) {
            switch (easing) {
                case LINEAR:
                    return t;
                case EASE_IN:
                    return t * t;
                case EASE_OUT:
                    return 1 - (1 - t) * (1 - t);
                case EASE_IN_OUT:
                    return t < 0.5 ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
                case BOUNCE:
                    return bounce(t);
                case ELASTIC:
                    return elastic(t);
                default:
                    return t;
            }
        }

        // 弹性缓动函数（放miku）
        private static float elastic(float t) {
            float c4 = (float)(2 * Math.PI) / 3;
            return t == 0 ? 0 : t == 1 ? 1 : (float)Math.pow(2, -10 * t) * (float)Math.sin((t * 10 - 0.75) * c4) + 1;
        }

        // 弹跳缓动函数（放miku）
        private static float bounce(float t) {
            if (t < 1 / 2.75f) {
                return 7.5625f * t * t;
            } else if (t < 2 / 2.75f) {
                t -= 1.5f / 2.75f;
                return 7.5625f * t * t + 0.75f;
            } else if (t < 2.5 / 2.75f) {
                t -= 2.25f / 2.75f;
                return 7.5625f * t * t + 0.9375f;
            } else {
                t -= 2.625f / 2.75f;
                return 7.5625f * t * t + 0.984375f;
            }
        }

        // 应用值到目标属性（放miku）
        private static void applyValue(Object target, String property, float value) {
            try {
                if (target instanceof Component) {
                    // 特殊处理常见属性（放miku）
                    if ("x".equalsIgnoreCase(property)) {
                        ((Component) target).setLocation((int) value, ((Component) target).getY());
                        return;
                    } else if ("y".equalsIgnoreCase(property)) {
                        ((Component) target).setLocation(((Component) target).getX(), (int) value);
                        return;
                    } else if ("width".equalsIgnoreCase(property)) {
                        ((Component) target).setSize((int) value, ((Component) target).getHeight());
                        return;
                    } else if ("height".equalsIgnoreCase(property)) {
                        ((Component) target).setSize(((Component) target).getWidth(), (int) value);
                        return;
                    } else if ("opacity".equalsIgnoreCase(property) && target instanceof Window) {
                        setWindowOpacity((Window) target, value);
                        return;
                    }
                }

                // 通用反射方法（放miku）
                Method setter = target.getClass().getMethod("set" + property, float.class);
                setter.invoke(target, value);
                if (target instanceof Component) {
                    ((Component) target).repaint();
                }
            } catch (Exception ex) {
                System.err.println("动画属性设置失败: " + property);
                ex.printStackTrace();
            }
        }

        // 设置窗口透明度（兼容不同JDK版本）
        private static void setWindowOpacity(Window window, float opacity) {
            try {
                Method setOpacity = Window.class.getMethod("setOpacity", float.class);
                setOpacity.invoke(window, Math.max(0, Math.min(1, opacity)));
            } catch (Exception e) {
                System.err.println("透明效果需要JDK7+");
            }
        }

        // 添加动画任务（放miku）
        private static void addAnimation(Object target, String property,
                                         float start, float end, long duration,
                                         Easing easing, Runnable onComplete,
                                         AnimationType type, boolean removeOnComplete) {
            AnimationTask task = new AnimationTask(
                    target, property, start, end, duration,
                    easing, onComplete, type, removeOnComplete
            );
            activeAnimations.put(target, task);
        }

        // 取消动画（放miku）
        public static void cancelAnimation(Object target) {
            activeAnimations.remove(target);
        }

        // 检查是否有动画在进行（放miku）
        public static boolean isAnimating(Object target) {
            return activeAnimations.containsKey(target);
        }


        public static void fadeIn(Window window, int duration) {
            fadeIn(window, duration, Easing.EASE_IN_OUT, null);
        }

        public static void fadeIn(Window window, int duration, Easing easing, Runnable onComplete) {
            setWindowOpacity(window, 0f);
            addAnimation(window, "opacity", 0f, 1f, duration, easing, onComplete, AnimationType.FADE_IN, true);
        }

        public static void fadeOut(Window window, int duration) {
            fadeOut(window, duration, Easing.EASE_IN_OUT, null);
        }

        public static void fadeOut(Window window, int duration, Easing easing, Runnable onComplete) {
            addAnimation(window, "opacity", 1f, 0f, duration, easing, () -> {
                if (onComplete != null) onComplete.run();
                window.dispose();
            }, AnimationType.FADE_OUT, true);
        }

        public static void slideIn(JComponent comp, int startX, int duration) {
            slideIn(comp, startX, comp.getY(), duration, Easing.EASE_OUT, null);
        }

        public static void slideIn(JComponent comp, int startX, int startY, int duration, Easing easing, Runnable onComplete) {
            comp.setLocation(startX, startY);
            addAnimation(comp, "x", startX, comp.getParent().getWidth()/2 - comp.getWidth()/2,
                    duration, easing, onComplete, AnimationType.SLIDE_IN, true);
            addAnimation(comp, "y", startY, comp.getParent().getHeight()/2 - comp.getHeight()/2,
                    duration, easing, null, AnimationType.SLIDE_IN, false);
        }

        public static void slideOut(JComponent comp, int endX, int endY, int duration, Easing easing, Runnable onComplete) {
            addAnimation(comp, "x", comp.getX(), endX, duration, easing, onComplete, AnimationType.SLIDE_OUT, true);
            addAnimation(comp, "y", comp.getY(), endY, duration, easing, null, AnimationType.SLIDE_OUT, false);
        }

        public static void scale(JComponent comp, float startScale, float endScale, int duration) {
            scale(comp, startScale, endScale, duration, Easing.EASE_IN_OUT, null);
        }

        public static void scale(JComponent comp, float startScale, float endScale,
                                 int duration, Easing easing, Runnable onComplete) {
            // 保存原始尺寸
            final int originalWidth = comp.getWidth();
            final int originalHeight = comp.getHeight();

            // 设置初始缩放
            comp.setSize((int)(originalWidth * startScale), (int)(originalHeight * startScale));

            addAnimation(comp, "width", originalWidth * startScale, originalWidth * endScale,
                    duration, easing, onComplete, AnimationType.SCALE, true);
            addAnimation(comp, "height", originalHeight * startScale, originalHeight * endScale,
                    duration, easing, null, AnimationType.SCALE, false);
        }

        public static void rotate(JComponent comp, float startAngle, float endAngle, int duration) {
            rotate(comp, startAngle, endAngle, duration, Easing.LINEAR, null);
        }

        public static void rotate(JComponent comp, float startAngle, float endAngle,
                                  int duration, Easing easing, Runnable onComplete) {
            // 使用自定义属性（放miku）
            comp.putClientProperty("rotation", startAngle);

            addAnimation(comp, "rotation", startAngle, endAngle, duration, easing, () -> {
                comp.putClientProperty("rotation", null);
                if (onComplete != null) onComplete.run();
            }, AnimationType.ROTATE, true);
        }

        // 自定义动画（播放miku）
        public static void animateProperty(Object target, String property,
                                           float start, float end, int duration,
                                           Easing easing, Runnable onComplete) {
            addAnimation(target, property, start, end, duration, easing, onComplete, AnimationType.CUSTOM, true);
        }
    }



    // 1. 组合滤镜（冰红茶混合口味）
    public static class CompositeFilter implements VideoFilter {
        private final List<VideoFilter> filters = new ArrayList<>();

        public void addFilter(VideoFilter filter) {
            filters.add(filter);
        }

        @Override
        public BufferedImage process(BufferedImage frame) {
            BufferedImage result = frame;
            for (VideoFilter filter : filters) {
                result = filter.process(result);
            }
            return result;
        }
    }

    // 2. 性能监控滤镜（冰红茶质量检测）
    public static class ProfilingFilter implements VideoFilter {
        private final String name;
        private long totalTime;
        private int frameCount;

        public ProfilingFilter(String name) {
            this.name = name;
        }

        @Override
        public BufferedImage process(BufferedImage frame) {
            long start = System.nanoTime();
            BufferedImage result = frame; // 本滤镜不修改图像
            long duration = System.nanoTime() - start;

            totalTime += duration;
            frameCount++;

            if (frameCount % 30 == 0) {
                double avgMs = (totalTime / frameCount) / 1_000_000.0;
                System.out.printf("[%s] 平均处理时间: %.2f ms (共%d帧)%n",
                        name, avgMs, frameCount);
            }

            return result;
        }
    }

    // 3. 参数化滤镜基类（可调节的冰红茶浓度）
    public static abstract class ParametrizedFilter implements VideoFilter {
        protected float intensity = 1.0f;

        public void setIntensity(float intensity) {
            this.intensity = Math.max(0, Math.min(2.0f, intensity));
        }
    }

    // 4. 模糊滤镜（朦胧的冰红茶）
    public static class BlurFilter extends ParametrizedFilter {
        @Override
        public BufferedImage process(BufferedImage src) {
            int radius = (int) (intensity * 5);
            if (radius < 1) return src;

            BufferedImage dst = new BufferedImage(
                    src.getWidth(), src.getHeight(), src.getType()
            );

            for (int y = radius; y < src.getHeight() - radius; y++) {
                for (int x = radius; x < src.getWidth() - radius; x++) {
                    int r = 0, g = 0, b = 0;
                    int count = 0;

                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dx = -radius; dx <= radius; dx++) {
                            Color c = new Color(src.getRGB(x + dx, y + dy));
                            r += c.getRed();
                            g += c.getGreen();
                            b += c.getBlue();
                            count++;
                        }
                    }

                    r /= count;
                    g /= count;
                    b /= count;
                    dst.setRGB(x, y, new Color(r, g, b).getRGB());
                }
            }
            return dst;
        }

        // 添加构造函数，接受模糊强度
        public BlurFilter(float intensity) {
            setIntensity(intensity); // 调用父类的方法设置强度
        }
    }

    // 5. 状态滤镜基类（随术曲变化的初音未来）
    public static abstract class StatefulFilter implements VideoFilter {
        protected long startTime = System.currentTimeMillis();

        protected float getElapsedSeconds() {
            return (System.currentTimeMillis() - startTime) / 1000.0f;
        }
    }

    // 6. 脉动波纹效果（跳动的冰红茶）
    public static class PulsatingRipple extends StatefulFilter {
        @Override
        public BufferedImage process(BufferedImage src) {
            float time = getElapsedSeconds();
            double amp = 3.0 + 2.0 * Math.sin(time * 2.0);
            return new RippleEffect(amp, 0.03).process(src);
        }
    }

    // 7. 区域滤镜（在一个地方撒了一点的冰红茶）
    public static class RegionFilter implements VideoFilter {
        private final VideoFilter delegate;
        private final Rectangle region;

        public RegionFilter(VideoFilter delegate, Rectangle region) {
            this.delegate = delegate;
            this.region = region;
        }

        @Override
        public BufferedImage process(BufferedImage src) {
            // 确保区域在图像范围内（放miku）
            Rectangle bounds = new Rectangle(0, 0, src.getWidth(), src.getHeight());
            Rectangle actualRegion = region.intersection(bounds);

            if (actualRegion.isEmpty()) return src;

            BufferedImage regionImage = src.getSubimage(
                    actualRegion.x, actualRegion.y,
                    actualRegion.width, actualRegion.height
            );

            BufferedImage processed = delegate.process(regionImage);

            Graphics2D g = src.createGraphics();
            g.drawImage(processed, actualRegion.x, actualRegion.y, null);
            g.dispose();

            return src;
        }
    }

    // 8. HSV色彩空间滤镜（miku被各式各样的p主整成多彩的发色）
    public static class HSVColorFilter implements VideoFilter {
        private float hueShift = 0;
        private float saturationFactor = 1;
        private float valueFactor = 1;

        public HSVColorFilter(float hueShift, float saturationFactor, float valueFactor) {
            this.hueShift = hueShift;
            this.saturationFactor = saturationFactor;
            this.valueFactor = valueFactor;
        }

        @Override
        public BufferedImage process(BufferedImage src) {
            BufferedImage dst = new BufferedImage(
                    src.getWidth(), src.getHeight(), src.getType()
            );

            for (int y = 0; y < src.getHeight(); y++) {
                for (int x = 0; x < src.getWidth(); x++) {
                    Color rgb = new Color(src.getRGB(x, y));
                    float[] hsv = Color.RGBtoHSB(
                            rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null
                    );

                    // 应用变换
                    hsv[0] = (hsv[0] + hueShift) % 1.0f;
                    hsv[1] = Math.min(1.0f, hsv[1] * saturationFactor);
                    hsv[2] = Math.min(1.0f, hsv[2] * valueFactor);

                    dst.setRGB(x, y, Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
                }
            }
            return dst;
        }
    }
    public static class MemoryMonitor {
        private static final long WARNING_THRESHOLD = 300 * 1024 * 1024; // 300MB

        public static void startMonitoring() {
            new Thread(() -> {
                while (true) {
                    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long maxMemory = Runtime.getRuntime().maxMemory();

                    System.out.printf("内存使用: %dMB / %dMB%n",
                            usedMemory / (1024 * 1024),
                            maxMemory / (1024 * 1024));

                    if (usedMemory > WARNING_THRESHOLD) {
                        System.out.println("警告：内存使用过高！");
                        // 可以触发更详细的内存分析或通知用户
                    }

                    try {
                        Thread.sleep(5000); // 每5秒检查一次
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Memory Monitor").start();
        }
    }
    public static class LeakDetector<T> {
        private final WeakReference<T> weakRef;

        public LeakDetector(T obj) {
            this.weakRef = new WeakReference<>(obj);
        }

        public boolean isLeaked() {
            return weakRef.get() != null;
        }

        public static void main(String[] args) {
            Object obj = new Object();
            LeakDetector<Object> detector = new LeakDetector<>(obj);

            obj = null; // 释放强引用

            System.gc(); // 提示JVM进行垃圾回收
            System.out.println("what can i say?泄漏或没有？");

            try {
                Thread.sleep(100); // 等待垃圾回收完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (detector.isLeaked()) {
                System.out.println("可能发生了内存泄漏！");//内存可以泄露，冰红茶不能漏
            }
        }
    }
    // 最简单的模块
    public static class InputDetector {
        private static InputDetector instance;
        private JFrame detectorFrame;
        private final Set<Integer> pressedKeys = new HashSet<>();
        private final Set<Integer> pressedMouseButtons = new HashSet<>();
        private int mouseWheelRotation = 0;

        private InputDetector() {}

        public static synchronized InputDetector getInstance() {
            if (instance == null) {
                instance = new InputDetector();
            }
            return instance;
        }

        public void startDetection() {
            if (detectorFrame != null && detectorFrame.isVisible()) {
                return;
            }

            detectorFrame = new JFrame();
            detectorFrame.setUndecorated(true);
            detectorFrame.setOpacity(0.01f); // 完全透明但可接收事件
            detectorFrame.setSize(1, 1);
            detectorFrame.setLocation(-10, -10); // 移出屏幕外
            detectorFrame.setAlwaysOnTop(true);
            detectorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            detectorFrame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    pressedKeys.add(e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    pressedKeys.remove(e.getKeyCode());
                }
            });

            detectorFrame.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pressedMouseButtons.add(e.getButton());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressedMouseButtons.remove(e.getButton());
                }
            });

            detectorFrame.addMouseWheelListener(e ->
                    mouseWheelRotation += e.getWheelRotation()
            );

            detectorFrame.setVisible(true);
        }

        public void stopDetection() {
            if (detectorFrame == null || !detectorFrame.isVisible()) {
                return; // 窗口不存在或未显示
            }

            detectorFrame.dispose();
            detectorFrame = null;

            pressedKeys.clear();
            pressedMouseButtons.clear();
            mouseWheelRotation = 0;
        }

        public boolean isDetectionActive() {
            return detectorFrame != null && detectorFrame.isVisible();
        }

        public boolean isKeyPressed(int keyCode) {
            return isDetectionActive() && pressedKeys.contains(keyCode);
        }

        public boolean isMouseButtonPressed(int button) {
            return isDetectionActive() && pressedMouseButtons.contains(button);
        }

        public int getMouseWheelRotation() {
            if (!isDetectionActive()) return 0;

            int rotation = mouseWheelRotation;
            mouseWheelRotation = 0; // 重置计数
            return rotation;
        }

        public int getMouseX() {
            return MouseInfo.getPointerInfo().getLocation().x;
        }

        public int getMouseY() {
            return MouseInfo.getPointerInfo().getLocation().y;
        }

        public Point getMousePosition() {
            return MouseInfo.getPointerInfo().getLocation();
        }
    }

    public class TextNumberConverter {//最和GUI没有关系的
        public static Long stringToNumber(String str) {
            if (str == null || str.isEmpty()) {
                return null;
            }

            long result = 0;
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                result = result * Integer.MAX_VALUE + c; // 使用Integer.MAX_VALUE作为基数
            }

            return result;
        }


        public static String numberToString(Long num) {
            if (num == null) {
                return null;
            }

            long number = num;
            StringBuilder result = new StringBuilder();

            if (number == 0) {
                return String.valueOf((char) 0);
            }

            while (number > 0) {
                long remainder = number % Integer.MAX_VALUE;
                result.insert(0, (char) remainder);
                number /= Integer.MAX_VALUE;
            }

            return result.toString();
        }


        private static final Map<String, Integer> CN_NUM = new HashMap<>();
        private static final Map<String, Integer> CN_UNIT = new HashMap<>();

        static {
            CN_NUM.put("零", 0);
            CN_NUM.put("一", 1);
            CN_NUM.put("二", 2);
            CN_NUM.put("两", 2);
            CN_NUM.put("三", 3);
            CN_NUM.put("四", 4);
            CN_NUM.put("五", 5);
            CN_NUM.put("六", 6);
            CN_NUM.put("七", 7);
            CN_NUM.put("八", 8);
            CN_NUM.put("九", 9);

            CN_UNIT.put("十", 10);
            CN_UNIT.put("百", 100);
            CN_UNIT.put("千", 1000);
            CN_UNIT.put("万", 10000);
            CN_UNIT.put("亿", 100000000);
        }

        // 文字转数字(放miku)
        public static Integer chineseToNumber(String chinese) {
            if (chinese == null || chinese.isEmpty()) return null;

            List<String> parts = new ArrayList<>();
            StringBuilder currentPart = new StringBuilder();
            boolean inUnitSection = false;

            for (String s : chinese.split("")) {
                if (CN_UNIT.containsKey(s)) {
                    if (!inUnitSection) {
                        if (currentPart.length() > 0) {
                            parts.add(currentPart.toString());
                            currentPart.setLength(0);
                        }
                        inUnitSection = true;
                    }
                    currentPart.append(s);
                } else {
                    if (inUnitSection) {
                        parts.add(currentPart.toString());
                        currentPart.setLength(0);
                        inUnitSection = false;
                    }
                    currentPart.append(s);
                }
            }
            if (currentPart.length() > 0) {
                parts.add(currentPart.toString());
            }

            List<Integer> numbers = new ArrayList<>();
            for (String part : parts) {
                Integer num = parsePart(part);
                if (num != null) {
                    numbers.add(num);
                }
            }

            if (numbers.isEmpty()) return null;

            return numbers.stream().reduce(0, (a, b) -> a + b);
        }

        private static Integer parsePart(String part) {
            List<Integer> numStack = new ArrayList<>();
            Integer current = 0;
            for (String s : part.split("")) {
                Integer num = CN_NUM.get(s);
                if (num != null) {
                    current = num;
                } else {
                    Integer unit = CN_UNIT.get(s);
                    if (unit != null) {
                        if (current == 0) {
                            current = 1;
                        }
                        if (!numStack.isEmpty() && numStack.get(numStack.size() - 1) > unit) {
                            current *= unit;
                            numStack.set(numStack.size() - 1, numStack.get(numStack.size() - 1) + current);
                        } else {
                            current *= unit;
                            if (!numStack.isEmpty()) {
                                current += numStack.get(numStack.size() - 1);
                                numStack.remove(numStack.size() - 1);
                            }
                        }
                        numStack.add(current);
                        current = 0;
                    }
                }
            }
            if (current > 0) {
                if (!numStack.isEmpty()) {
                    current += numStack.get(numStack.size() - 1);
                    numStack.remove(numStack.size() - 1);
                }
                numStack.add(current);
            }
            return numStack.isEmpty() ? null : numStack.get(numStack.size() - 1);
        }

        public static String numberToChinese(Integer number) {
            if (number == null) return null;

            String[] numUnit = {"", "十", "百", "千"};
            String[] numChinese = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
            String[] bigUnit = {"", "万", "亿", "万亿"};//这些是单位和数字(放miku)

            if (number == 0) return "零";

            String result = "";
            int unitIndex = 0;

            while (number > 0) {
                int part = number % 10000;
                number /= 10000;

                String partResult = "";
                boolean hasNonZero = false;

                for (int i = 0; i < 4; i++) {
                    int num = part % 10;
                    part /= 10;
                    if (num == 0) {
                        if (hasNonZero) {
                            partResult = numChinese[0] + partResult;
                        }
                    } else {
                        hasNonZero = true;
                        partResult = numChinese[num] + (i > 0 ? numUnit[i] : "") + partResult;
                    }
                }

                if (hasNonZero) {
                    partResult += bigUnit[unitIndex];
                    result = partResult + result;
                } else if (result.length() > 0) {
                    result = numChinese[0] + result;
                }

                unitIndex++;
            }

            return result.replaceAll("一十", "十").replaceAll("零+", "零").replaceAll("零$", "");
            //解放了！(放miku)
        }
    }

    public final class MathUtils {
        private MathUtils() {}

        public static double abs(double a) { return Math.abs(a); }
        public static int max(int a, int b) { return Math.max(a, b); }
        public static double min(double a, double b) { return Math.min(a, b); }
        public static double exp(double a) { return Math.exp(a); }
        public static double log(double a) { return Math.log(a); }
        public static double log10(double a) { return Math.log10(a); }
        public static double pow(double a, double b) { return Math.pow(a, b); }
        public static double sqrt(double a) { return Math.sqrt(a); }
        public static double cbrt(double a) { return Math.cbrt(a); }
        public static double ceil(double a) { return Math.ceil(a); }
        public static double floor(double a) { return Math.floor(a); }
        public static long round(double a) { return Math.round(a); }
        public static double rint(double a) { return Math.rint(a); }
        public static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        public static double sin(double a) { return Math.sin(a); }
        public static double cos(double a) { return Math.cos(a); }
        public static double tan(double a) { return Math.tan(a); }
        public static double asin(double a) { return Math.asin(a); }
        public static double acos(double a) { return Math.acos(a); }
        public static double atan(double a) { return Math.atan(a); }
        public static double atan2(double y, double x) { return Math.atan2(y, x); }
        public static double sinh(double x) { return Math.sinh(x); }
        public static double cosh(double x) { return Math.cosh(x); }
        public static double tanh(double x) { return Math.tanh(x); }

        public static double angleToOrigin(double x, double y) {
            return Math.atan2(y, x);
        }

        public static double angleToOriginDegrees(double x, double y) {
            return Math.toDegrees(angleToOrigin(x, y));
        }

        public static double angleBetweenPoints(double x1, double y1, double x2, double y2) {
            return Math.atan2(y2 - y1, x2 - x1);
        }

        public static double angleBetweenPointsDegrees(double x1, double y1, double x2, double y2) {
            return Math.toDegrees(angleBetweenPoints(x1, y1, x2, y2));
        }

        public static double[] directionVector(double x1, double y1, double x2, double y2) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx*dx + dy*dy);
            return new double[]{dx/length, dy/length};
        }

        public static double distance(double x1, double y1, double x2, double y2) {
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        public static double dotProduct(double x1, double y1, double x2, double y2) {
            return x1 * x2 + y1 * y2;
        }

        public static double crossProduct(double x1, double y1, double x2, double y2) {
            return x1 * y2 - y1 * x2;
        }

        public static double angleBetweenVectors(double x1, double y1, double x2, double y2) {
            double dot = x1*x2 + y1*y2;
            double mag1 = Math.sqrt(x1*x1 + y1*y1);
            double mag2 = Math.sqrt(x2*x2 + y2*y2);
            return Math.acos(dot / (mag1 * mag2));
        }

        public static double[] rotatePoint(double x, double y, double angle) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            return new double[]{x * cos - y * sin, x * sin + y * cos};
        }

        public static double[] centroid(double[]... points) {
            double sumX = 0, sumY = 0;
            for (double[] p : points) {
                sumX += p[0];
                sumY += p[1];
            }
            return new double[]{sumX/points.length, sumY/points.length};
        }

        private static final double EPSILON = 1e-10;

        public static double[] lineSegmentIntersection(
                double x1, double y1, double x2, double y2,
                double x3, double y3, double x4, double y4)
        {
            if (!hasBoundingBoxOverlap(x1, y1, x2, y2, x3, y3, x4, y4)) {
                return null;
            }

            final double dx12 = x2 - x1;
            final double dy12 = y2 - y1;
            final double dx34 = x4 - x3;
            final double dy34 = y4 - y3;

            final double crossProduct = dx12 * dy34 - dx34 * dy12;

            if (Math.abs(crossProduct) < EPSILON) {
                return handleColinearSegments(x1, y1, x2, y2, x3, y3, x4, y4);
            }

            final double relativeX = x3 - x1;
            final double relativeY = y3 - y1;

            final double t = (relativeX * dy34 - relativeY * dx34) / crossProduct;
            final double s = (relativeX * dy12 - relativeY * dx12) / crossProduct;

            if (isValidParameter(t) && isValidParameter(s)) {
                final double ix = x1 + t * dx12;
                final double iy = y1 + t * dy12;

                if (isOnBothSegments(ix, iy, x1, y1, x2, y2, x3, y3, x4, y4)) {
                    return new double[]{ix, iy};
                }
            }
            return null;
        }

        public static double lerp(double start, double end, double t) {
            return start + t * (end - start);
        }

        public static double easeInOut(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        }

        public static double mean(double... values) {
            double sum = 0;
            for (double v : values) sum += v;
            return sum / values.length;
        }

        public static double standardDeviation(double... values) {
            double mean = mean(values);
            double sum = 0;
            for (double v : values) sum += Math.pow(v - mean, 2);
            return Math.sqrt(sum / values.length);
        }

        public static double[] cartesianToPolar(double x, double y) {
            double r = Math.sqrt(x*x + y*y);
            double theta = Math.atan2(y, x);
            return new double[]{r, theta};
        }

        public static double[] polarToCartesian(double r, double theta) {
            return new double[]{r * Math.cos(theta), r * Math.sin(theta)};
        }

        public static double degreesToRadians(double deg) {
            return deg * Math.PI / 180.0;
        }

        public static double radiansToDegrees(double rad) {
            return rad * 180.0 / Math.PI;
        }

        public static double random() { return Math.random(); }
        public static int randomInt(int bound) { return (int) (Math.random() * bound); }
        public static int randomRange(int min, int max) {
            return min + randomInt(max - min + 1);
        }
        public static double randomGaussian() {
            return new Random().nextGaussian();
        }

        public static double[][] matrixAdd(double[][] a, double[][] b) {
            int rows = a.length, cols = a[0].length;
            double[][] result = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[i][j] = a[i][j] + b[i][j];
                }
            }
            return result;
        }

        public static double[][] matrixMultiply(double[][] a, double[][] b) {
            int aRows = a.length, aCols = a[0].length;
            int bCols = b[0].length;
            double[][] result = new double[aRows][bCols];
            for (int i = 0; i < aRows; i++) {
                for (int j = 0; j < bCols; j++) {
                    for (int k = 0; k < aCols; k++) {
                        result[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
            return result;
        }

        public static double[][] matrixTranspose(double[][] matrix) {
            int rows = matrix.length;
            int cols = matrix[0].length;
            double[][] result = new double[cols][rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[j][i] = matrix[i][j];
                }
            }
            return result;
        }

        private static boolean hasBoundingBoxOverlap(double x1, double y1, double x2, double y2,
                                                     double x3, double y3, double x4, double y4) {
            final double minX1 = Math.min(x1, x2);
            final double maxX1 = Math.max(x1, x2);
            final double minY1 = Math.min(y1, y2);
            final double maxY1 = Math.max(y1, y2);

            final double minX2 = Math.min(x3, x4);
            final double maxX2 = Math.max(x3, x4);
            final double minY2 = Math.min(y3, y4);
            final double maxY2 = Math.max(y3, y4);

            return (maxX1 >= minX2 - EPSILON) && (minX1 <= maxX2 + EPSILON) &&
                    (maxY1 >= minY2 - EPSILON) && (minY1 <= maxY2 + EPSILON);
        }

        private static double[] handleColinearSegments(double x1, double y1, double x2, double y2,
                                                       double x3, double y3, double x4, double y4) {
            if (isSinglePoint(x1, y1, x2, y2)) {
                return isOnSegment(x1, y1, x3, y3, x4, y4) ? new double[]{x1, y1} : null;
            }
            if (isSinglePoint(x3, y3, x4, y4)) {
                return isOnSegment(x3, y3, x1, y1, x2, y2) ? new double[]{x3, y3} : null;
            }

            final boolean xOverlap = hasAxisOverlap(x1, x2, x3, x4);
            final boolean yOverlap = hasAxisOverlap(y1, y2, y3, y4);

            return (xOverlap && yOverlap) ? findMidpoint(x1, x2, x3, x4, y1, y2, y3, y4) : null;
        }

        private static boolean hasAxisOverlap(double a1, double a2, double b1, double b2) {
            final double minA = Math.min(a1, a2);
            final double maxA = Math.max(a1, a2);
            final double minB = Math.min(b1, b2);
            final double maxB = Math.max(b1, b2);
            return (maxA >= minB - EPSILON) && (minA <= maxB + EPSILON);
        }

        private static double[] findMidpoint(double x1, double x2, double x3, double x4,
                                             double y1, double y2, double y3, double y4) {
            final double[] xRange = getOverlapRange(x1, x2, x3, x4);
            final double[] yRange = getOverlapRange(y1, y2, y3, y4);
            return new double[]{(xRange[0] + xRange[1]) / 2, (yRange[0] + yRange[1]) / 2};
        }

        private static double[] getOverlapRange(double a1, double a2, double b1, double b2) {
            final double start = Math.max(Math.min(a1, a2), Math.min(b1, b2));
            final double end = Math.min(Math.max(a1, a2), Math.max(b1, b2));
            return (start > end + EPSILON) ? null : new double[]{start, end};
        }

        private static boolean isOnBothSegments(double x, double y,
                                                double x1, double y1, double x2, double y2,
                                                double x3, double y3, double x4, double y4) {
            return isOnSegment(x, y, x1, y1, x2, y2) &&
                    isOnSegment(x, y, x3, y3, x4, y4);
        }

        private static boolean isOnSegment(double px, double py,
                                           double x1, double y1, double x2, double y2) {
            if (isSamePoint(px, py, x1, y1) || isSamePoint(px, py, x2, y2)) return true;
            if (Math.abs((px - x1) * (y2 - y1) - (py - y1) * (x2 - x1)) > EPSILON) return false;
            return isBetween(px, x1, x2) && isBetween(py, y1, y2);
        }

        private static boolean isSamePoint(double x1, double y1, double x2, double y2) {
            return Math.abs(x1 - x2) < EPSILON && Math.abs(y1 - y2) < EPSILON;
        }

        private static boolean isSinglePoint(double x1, double y1, double x2, double y2) {
            return isSamePoint(x1, y1, x2, y2);
        }

        private static boolean isValidParameter(double t) {
            return t >= -EPSILON && t <= 1.0 + EPSILON;
        }

        private static boolean isBetween(double val, double end1, double end2) {
            final double min = Math.min(end1, end2);
            final double max = Math.max(end1, end2);
            return val >= min - EPSILON && val <= max + EPSILON;
        }

        public static final double PI = Math.PI;
        public static final double E = Math.E;
        public static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
        public static final double DEG_TO_RAD = PI / 180.0;
        public static final double RAD_TO_DEG = 180.0 / PI;
    }
    public static class Physics {
        public static boolean isCollisionRectCircle(double rectX, double rectY, double rectWidth, double rectHeight, double circleX, double circleY, double radius) {
            double closestX = Math.max(rectX, Math.min(circleX, rectX + rectWidth));
            double closestY = Math.max(rectY, Math.min(circleY, rectY + rectHeight));

            double distanceSquared = Math.pow(circleX - closestX, 2) + Math.pow(circleY - closestY, 2);

            return distanceSquared <= Math.pow(radius, 2);
        }

        public static boolean isCollisionCircleCircle(double x1, double y1, double r1, double x2, double y2, double r2) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double distanceSquared = dx * dx + dy * dy;
            double radiiSum = r1 + r2;
            return distanceSquared <= radiiSum * radiiSum;
        }

        public static double[] lineSegmentIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
            double det = (x2 - x1) * (y4 - y3) - (y2 - y1) * (x4 - x3);

            if (det == 0) return null; // 线段平行

            double t = ((x3 - x1) * (y4 - y3) - (y3 - y1) * (x4 - x3)) / det;
            double s = ((x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1)) / det;

            if (t >= 0 && t <= 1 && s >= 0 && s <= 1) {
                return new double[]{x1 + t * (x2 - x1), y1 + t * (y2 - y1)};
            }
            return null;
        }

        public static double[] applyForce(double mass, double[] velocity, double[] force, double deltaTime) {
            double accelerationX = force[0] / mass;
            double accelerationY = force[1] / mass;

            velocity[0] += accelerationX * deltaTime;
            velocity[1] += accelerationY * deltaTime;

            return velocity;
        }

        public static double[] updatePosition(double[] position, double[] velocity, double deltaTime) {
            position[0] += velocity[0] * deltaTime;
            position[1] += velocity[1] * deltaTime;
            return position;
        }

        public static double calculateKineticEnergy(double mass, double speed) {
            return 0.5 * mass * speed * speed;
        }

        public static double calculatePotentialEnergy(double mass, double height, double gravity) {
            return mass * gravity * height;
        }

        public static double[] reflectVector(double[] velocity, double[] normal) {
            double dotProduct = velocity[0] * normal[0] + velocity[1] * normal[1];
            return new double[]{
                    velocity[0] - 2 * dotProduct * normal[0],
                    velocity[1] - 2 * dotProduct * normal[1]
            };
        }

        public static double[] applyFriction(double[] velocity, double frictionCoefficient, double deltaTime) {
            double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);
            if (speed < 0.1) return new double[]{0, 0}; // 速度过低则停止

            double frictionMagnitude = frictionCoefficient * speed;
            double frictionX = -frictionMagnitude * velocity[0] / speed;
            double frictionY = -frictionMagnitude * velocity[1] / speed;

            return new double[]{
                    velocity[0] + frictionX * deltaTime,
                    velocity[1] + frictionY * deltaTime
            };
        }

        public static void applyAirResistance(Vector2f velocity, float airDensity, float dragCoefficient, float crossSectionArea, Vector2f force) {
            float speedSquared = velocity.lengthSq();

            if (speedSquared > 0.01f) {
                float dragMagnitude = 0.5f * airDensity * speedSquared * dragCoefficient * crossSectionArea;
                Vector2f drag = velocity.normalized().mul(-dragMagnitude);
                force.x += drag.x;
                force.y += drag.y;
            }
        }

        public static Vector2f calculateFrictionForce(float mass, float frictionCoefficient, Vector2f velocity) {
            float normalForce = mass * 9.81f;
            if (velocity.lengthSq() < 0.1f) {
                return new Vector2f();
            } else {
                float kineticFriction = normalForce * frictionCoefficient * 0.8f;
                return velocity.normalized().mul(-kineticFriction);
            }
        }

        public static void updateKinematics(Vector2f position, Vector2f velocity, Vector2f force, float mass, float deltaTime) {
            Vector2f acceleration = force.div(mass);
            velocity.addi(acceleration.mul(deltaTime));
            position.addi(velocity.mul(deltaTime));
        }

        public static void resolvePlayerCollision(Vector2f aPos, Vector2f aVel, float aMass,
                                                  Vector2f bPos, Vector2f bVel, float bMass) {
            Vector2f collisionNormal = aPos.sub(bPos).normalized();
            float relativeSpeed = bVel.sub(aVel).dot(collisionNormal);
            float impulse = (2 * relativeSpeed) / (1/aMass + 1/bMass);

            aVel.addi(collisionNormal.mul(impulse / aMass));
            bVel.subi(collisionNormal.mul(impulse / bMass));
        }

        public static Vector2f calculateSurfaceNormal(Line2D line) {
            double dx = line.getX2() - line.getX1();
            double dy = line.getY2() - line.getY1();
            return new Vector2f((float)-dy, (float)dx).normalized();
        }

        public static class Vector2f {
            public float x, y;
            private static final float EPSILON = 1e-7f;

            public Vector2f() { this(0, 0); }
            public Vector2f(float x, float y) {
                this.x = x;
                this.y = y;
            }

            public Vector2f add(Vector2f o) { return new Vector2f(x + o.x, y + o.y); }
            public Vector2f sub(Vector2f o) { return new Vector2f(x - o.x, y - o.y); }
            public Vector2f mul(float s) { return new Vector2f(x * s, y * s); }
            public Vector2f div(float s) { return s == 0 ? new Vector2f() : new Vector2f(x / s, y / s); }
            public float length() { return (float) Math.sqrt(x * x + y * y); }
            public float lengthSq() { return x * x + y * y; }
            public Vector2f normalized() {
                float len = length();
                return len > EPSILON ? div(len) : new Vector2f();
            }
            public float dot(Vector2f o) { return x * o.x + y * o.y; }
            public void set(float x, float y) { this.x = x; this.y = y; }
            public void addi(Vector2f o) { x += o.x; y += o.y; }
            public void subi(Vector2f o) { x -= o.x; y -= o.y; }
            public void muli(float s) { x *= s; y *= s; }
            public void divi(float s) { if (s != 0) { x /= s; y /= s; } }
        }
    }
    //往GUI框架里塞我蒸馏后的往年的物理引擎和常用的数学运算，我简直是“天（sha）才（bi）”（没有抽象注释的原因）来都来了，再加个动画引擎怎么样...不对，好像已经加了
}
//希望你调出适合你喝的冰红茶，听适合听的术
//try {
//    用户.发送救心丸(); // 调用药房API（剂量：400mg）
//    推送meme缓释片("程序员迷惑行为大赏.gif");
//} catch (笑到缺氧异常 e) {
//    启动心肺复苏协议();
//    System.out.println("⚠️警告：建议对屏幕使用防喷膜！");
//} finally {
//    播放安抚语音("术力口~術~術~（电子观音普度众生版）");
//}
//hh，花这么多时间，这么多精力，搞了个3000行的框架，hhh
//不要看亮了这么多黄灯，实际上都是精华
