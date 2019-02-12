import java.io.*;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Random;
import java.lang.Math;

public class Lab5 extends Component implements ActionListener {

    // ************************************
    // List of the options(Original, Negative); correspond to the cases:
    // ************************************

    String descs[] = { "Original", "Negative", "Shift", "Add", "Subtract", "Multiply", "Divide", "Bitwise Not",
            "Bitwise And", "Bitwise Or", "Bitwise XOR", "ROI", "Log", "Power", "LUT", "Bit Plane Slice", 
            "Histogram" };

    int opIndex;
    int lastOp;

    private BufferedImage bi, biFiltered, diFiltered, secondImage, ROIImage; // the input image saved as bi;//
    int w, h, secondImgWidth, secondImageHeight;
    boolean noise = false; // create noise
    float scalar = 1;
    int t = -10;
    int[] LUT = generateLUT(30);
    int bitPlanes = 7;

    public Lab5() {
        try {
            bi = ImageIO.read(new File("Barbara.bmp"));
            w = bi.getWidth(null);
            h = bi.getHeight(null);
            System.out.println(bi.getType());
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;
                diFiltered = bi = bi2;
            }
            secondImage = ImageIO.read(new File("Lena.bmp"));
            ROIImage = ImageIO.read(new File("ROI.png"));

        } catch (IOException e) { // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");
            System.exit(1);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = { "bmp", "gif", "jpeg", "jpg", "png" };
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }

    void setOpIndex(int i) {
        opIndex = i;
    }

    public void paint(Graphics g) { // Repaint will call this function so the image will change.
        filterImage();
        g.drawImage(diFiltered, w, 0, null);
        g.drawImage(biFiltered, 0, 0, null);
    }

    // ************************************
    // Convert the Buffered Image to Array
    // ************************************
    private static int[][][] convertToArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] result = new int[width][height][4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                result[x][y][0] = a;
                result[x][y][1] = r;
                result[x][y][2] = g;
                result[x][y][3] = b;
            }
        }
        return result;
    }

    // ************************************
    // Convert the Array to BufferedImage
    // ************************************
    public BufferedImage convertToBimage(int[][][] TmpArray) {

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];

                // set RGB value

                int p = (a << 24) | (r << 16) | (g << 8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }
        return tmpimg;
    }

    // ************************************
    // Example: Image Negative
    // ************************************
    public BufferedImage ImageNegative(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg); // Convert the image to array

        // Image Negative Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArray[x][y][1] = 255 - ImageArray[x][y][1]; // r
                ImageArray[x][y][2] = 255 - ImageArray[x][y][2]; // g
                ImageArray[x][y][3] = 255 - ImageArray[x][y][3]; // b
            }
        }

        return convertToBimage(ImageArray); // Convert the array to BufferedImage
    }

    // ************************************
    // Your turn now: Add more function below
    // ************************************

    // shift and scale
    public BufferedImage rescale(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);
        int[][][] ImageArrayNew = new int[width][height][4];
        Random rand = new Random();

        int n = 0;
        int rmin = (int) scalar * (ImageArray[0][0][1] + t);
        int rmax = rmin;
        int gmin = (int) scalar * (ImageArray[0][0][2] + t);
        int gmax = gmin;
        int bmin = (int) scalar * (ImageArray[0][0][3] + t);
        int bmax = bmin;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (noise) {
                    n = rand.nextInt(100) + 1;
                }
                ImageArrayNew[x][y][1] = (int) scalar * (ImageArray[x][y][1] + t); // r
                if (noise) {
                    n = rand.nextInt(100) + 1;
                }
                ImageArrayNew[x][y][2] = (int) scalar * (ImageArray[x][y][2] + t); // g
                if (noise) {
                    n = rand.nextInt(100) + 1;
                }
                ImageArrayNew[x][y][3] = (int) scalar * (ImageArray[x][y][3] + t); // b
                if (rmin > ImageArrayNew[x][y][1]) {
                    rmin = ImageArrayNew[x][y][1];
                }
                if (gmin > ImageArrayNew[x][y][2]) {
                    gmin = ImageArrayNew[x][y][2];
                }
                if (bmin > ImageArrayNew[x][y][3]) {
                    bmin = ImageArrayNew[x][y][3];
                }
                if (rmax < ImageArrayNew[x][y][1]) {
                    rmax = ImageArrayNew[x][y][1];
                }
                if (gmax < ImageArrayNew[x][y][2]) {
                    gmax = ImageArrayNew[x][y][2];
                }
                if (bmax < ImageArrayNew[x][y][3]) {
                    bmax = ImageArrayNew[x][y][3];
                }
            }
        }
        // conditional
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArrayNew[x][y][1] = 255 * (ImageArrayNew[x][y][1] - rmin) / (rmax - rmin);
                ImageArrayNew[x][y][2] = 255 * (ImageArrayNew[x][y][2] - gmin) / (gmax - gmin);
                ImageArrayNew[x][y][3] = 255 * (ImageArrayNew[x][y][3] - bmin) / (bmax - bmin);
                if (ImageArrayNew[x][y][1] < 0) {
                    ImageArrayNew[x][y][1] = 0;
                }
                if (ImageArrayNew[x][y][1] > 255) {
                    ImageArrayNew[x][y][1] = 255;
                }
                if (ImageArrayNew[x][y][2] < 0) {
                    ImageArrayNew[x][y][2] = 0;
                }
                if (ImageArrayNew[x][y][2] > 255) {
                    ImageArrayNew[x][y][2] = 255;
                }
                if (ImageArrayNew[x][y][3] < 0) {
                    ImageArrayNew[x][y][3] = 0;
                }
                if (ImageArrayNew[x][y][3] > 255) {
                    ImageArrayNew[x][y][3] = 255;
                }
            }
        }
        return convertToBimage(ImageArrayNew);
    }

    public BufferedImage add(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);

        int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        // add mtx elements, save min and max of each RGB component
        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                for (int z = 1; z < 4; z++) {
                    returnMtx[x][y][z] = mtxA[x][y][z] + mtxB[x][y][z];
                }
            }
        }
        return rescale(convertToBimage(returnMtx));
    }

    public BufferedImage subtract(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        // subtract mtx elements, save min and max of each RGB component
        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                for (int z = 0; z < 4; z++) {
                    returnMtx[x][y][z] = mtxB[x][y][z] - mtxA[x][y][z];
                }
            }
        }
        return rescale(convertToBimage(returnMtx));
    }

    public BufferedImage multiply(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        int[][] tmpMtx = new int[imgA.getHeight()][imgA.getHeight()];
        int[][][] returnMtx = new int[imgA.getHeight()][imgA.getHeight()][4];

        if (imgA.getWidth() != imgB.getHeight()) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }

        // matrix multiplication
        for (int i = 0; i < imgA.getWidth(); i++) {
            for (int j = 0; j < imgB.getHeight(); j++) {
                for (int k = 0; k < imgA.getHeight(); k++) {
                    tmpMtx[i][j] += mtxA[i][k][1] * mtxB[k][j][1];
                }
            }
        }

        // convert grayscale back to color
        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getHeight(); x++) {
                returnMtx[x][y][1] = tmpMtx[x][y]; // r
                returnMtx[x][y][2] = tmpMtx[x][y]; // g
                returnMtx[x][y][3] = tmpMtx[x][y]; // b
            }
        }
        return rescale(convertToBimage(returnMtx));
    }

    public BufferedImage divide(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        int[][][] returnMtx = new int[imgA.getHeight()][imgA.getHeight()][4];

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                for (int z = 0; z < 4; z++) {
                    returnMtx[x][y][z] = mtxB[x][y][z] / mtxA[x][y][z];
                }
            }
        }
        return rescale(convertToBimage(returnMtx));
    }

    public BufferedImage bitwiseNot(BufferedImage imgA, BufferedImage imgB) {

        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        // int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                int r = mtxA[x][y][1]; // r
                int g = mtxA[x][y][2]; // g
                int b = mtxA[x][y][3]; // b
                mtxB[x][y][1] = (~r) & 0xFF; // r
                mtxB[x][y][2] = (~g) & 0xFF; // g
                mtxB[x][y][3] = (~b) & 0xFF; // b
            }
        }
        return rescale(convertToBimage(mtxB));
    }

    public BufferedImage bitwiseAnd(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        // int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                int r = mtxA[x][y][1]; // r
                int g = mtxA[x][y][2]; // g
                int b = mtxA[x][y][3]; // b
                mtxB[x][y][1] = (mtxB[x][y][1] & r) & 0xFF; // r
                mtxB[x][y][2] = (mtxB[x][y][2] & g) & 0xFF; // g
                mtxB[x][y][3] = (mtxB[x][y][3] & b) & 0xFF; // b
            }
        }
        return rescale(convertToBimage(mtxB));
    }

    public BufferedImage bitwiseOr(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        // int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                int r = mtxA[x][y][1]; // r
                int g = mtxA[x][y][2]; // g
                int b = mtxA[x][y][3]; // b
                mtxB[x][y][1] = (mtxB[x][y][1] | r) & 0xFF; // r
                mtxB[x][y][2] = (mtxB[x][y][2] | g) & 0xFF; // g
                mtxB[x][y][3] = (mtxB[x][y][3] | b) & 0xFF; // b
            }
        }
        return rescale(convertToBimage(mtxB));
    }

    public BufferedImage bitwiseXOR(BufferedImage imgA, BufferedImage imgB) {
        int[][][] mtxA = convertToArray(imgA);
        int[][][] mtxB = convertToArray(imgB);
        // int[][][] returnMtx = new int[imgA.getWidth()][imgA.getHeight()][4];

        // check image dimensions, cannot add mtx of diff. dimensions
        if ((imgA.getWidth() != imgB.getWidth()) || (imgA.getHeight() != imgB.getHeight())) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                int r = mtxA[x][y][1]; // r
                int g = mtxA[x][y][2]; // g
                int b = mtxA[x][y][3]; // b
                mtxB[x][y][1] = (mtxB[x][y][1] ^ r) & 0xFF; // r
                mtxB[x][y][2] = (mtxB[x][y][2] ^ g) & 0xFF; // g
                mtxB[x][y][3] = (mtxB[x][y][3] ^ b) & 0xFF; // b
            }
        }
        return rescale(convertToBimage(mtxB));
    }

    public BufferedImage log(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg); // Convert the image to array
        double c = 255 / (Math.log(256.0)); // preserve range 0->255
        // Image Log Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArray[x][y][1] = (int) (1 + (c * Math.log(ImageArray[x][y][1]))); // r
                ImageArray[x][y][2] = (int) (1 + (c * Math.log(ImageArray[x][y][2]))); // g
                ImageArray[x][y][3] = (int) (1 + (c * Math.log(ImageArray[x][y][3]))); // b
            }
        }
        return rescale(convertToBimage(ImageArray)); // Convert the array to BufferedImage
    }

    public BufferedImage power(BufferedImage timg, float power) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg); // Convert the image to array

        double c = 255 / Math.pow(255, power); // preserve range 0->255
        // Image Power Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArray[x][y][1] = (int) (c * Math.pow(ImageArray[x][y][1], power)); // r
                ImageArray[x][y][2] = (int) (c * Math.pow(ImageArray[x][y][2], power)); // g
                ImageArray[x][y][3] = (int) (c * Math.pow(ImageArray[x][y][3], power)); // b
            }
        }
        return rescale(convertToBimage(ImageArray)); // Convert the array to BufferedImage
    }

    public BufferedImage LUT(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray1 = convertToArray(timg); // Convert the image to array
        int[][][] ImageArray2 = new int[width][height][4]; // Convert the image to array

        // Image LUT Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = ImageArray1[x][y][1]; // r
                int g = ImageArray1[x][y][2]; // g
                int b = ImageArray1[x][y][3]; // b
                ImageArray2[x][y][1] = LUT[r]; // r
                ImageArray2[x][y][2] = LUT[g]; // g
                ImageArray2[x][y][3] = LUT[b]; // b
            }
        }
        return rescale(convertToBimage(ImageArray2)); // Convert the array to BufferedImage
    }

    public int[] generateLUT(int a) {
        int[] LUT = new int[256];

        for (int i = 0; i < 245; i++) {
            LUT[i] = i + a;
        }
        return LUT;
    }

    public BufferedImage bitPlaneSlice(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray1 = convertToArray(timg); // Convert the image to array
        int[][][] ImageArray2 = new int[width][height][4]; // Convert the image to array

        // Image LUT Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = ImageArray1[x][y][1]; // r
                int g = ImageArray1[x][y][2]; // g
                int b = ImageArray1[x][y][3]; // b
                ImageArray2[x][y][1] = (r >> bitPlanes) & 1; // r
                ImageArray2[x][y][2] = (g >> bitPlanes) & 1; // g
                ImageArray2[x][y][3] = (b >> bitPlanes) & 1; // b
            }
        }
        return rescale(convertToBimage(ImageArray2)); // Convert the array to BufferedImage
    }

    // To find the histograms for RGB components of an image
    public BufferedImage histogram(BufferedImage timg) {

        int width = timg.getWidth();
        int height = timg.getHeight();

        float[] HistogramR = new float[256];
        float[] HistogramG = new float[256];
        float[] HistogramB = new float[256];
        int[][][] ImageArray = convertToArray(timg); // Convert the image to array

        for (int k = 0; k <= 255; k++) { // Initialisation
            HistogramR[k] = 0;
            HistogramG[k] = 0;
            HistogramB[k] = 0;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = ImageArray[x][y][1]; // r
                int g = ImageArray[x][y][2]; // g
                int b = ImageArray[x][y][3]; // b
                HistogramR[r]++;
                System.out.println("Setting histogram " + HistogramR[r]);
                HistogramG[g]++;
                HistogramB[b]++;
            }
        }
        //normalise
        for (int k = 0; k <= 255; k++) { 
            HistogramR[k] /= width * height;
            HistogramG[k] /= width * height;
            HistogramB[k] /= width * height;
        }
        //cumulative distribution
        for (int k = 1; k <= 255; k++) { 
            HistogramR[k] += HistogramR[k - 1];
            HistogramG[k] += HistogramR[k - 1];
            HistogramB[k] += HistogramR[k - 1];
        }
        //multiply cumulative values by maximum value 255
        for (int k = 0; k <= 255; k++) { 
            HistogramR[k] *= 255;
            HistogramG[k] *= 255;
            HistogramB[k] *= 255;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArray[x][y][1] = (int)HistogramR[ImageArray[x][y][1]]; // r
                ImageArray[x][y][2] = (int)HistogramR[ImageArray[x][y][2]]; // g
                ImageArray[x][y][3] = (int)HistogramR[ImageArray[x][y][3]]; // b
            }
        }
        return convertToBimage(ImageArray);
    }

    // ************************************
    // You need to register your function here
    // ************************************
    public void filterImage() {

        if (opIndex == lastOp) {
            return;
        }

        lastOp = opIndex;
        switch (opIndex) {
        case 0:
            biFiltered = bi; /* original */
            diFiltered = bi;
            return;
        case 1:
            biFiltered = bi; /* Image Negative */
            diFiltered = ImageNegative(bi);
            return;
        case 2:
            biFiltered = bi;
            diFiltered = rescale(bi);
            return;
        case 3:
            biFiltered = bi;
            diFiltered = add(bi, secondImage);
            return;
        case 4:
            biFiltered = bi;
            diFiltered = subtract(bi, secondImage);
            return;
        case 5:
            biFiltered = bi;
            diFiltered = multiply(bi, secondImage);
            return;
        case 6:
            biFiltered = bi;
            diFiltered = divide(bi, secondImage);
            return;
        case 7:
            biFiltered = bi;
            diFiltered = bitwiseNot(bi, secondImage);
            return;
        case 8:
            biFiltered = bi;
            diFiltered = bitwiseAnd(bi, secondImage);
            return;
        case 9:
            biFiltered = bi;
            diFiltered = bitwiseOr(bi, secondImage);
            return;
        case 10:
            biFiltered = bi;
            diFiltered = bitwiseXOR(bi, secondImage);
            return;
        case 11:
            biFiltered = bi;
            diFiltered = bitwiseAnd(bi, ROIImage);
            return;
        case 12:
            biFiltered = bi;
            diFiltered = log(bi);
            return;
        case 13:
            biFiltered = bi;
            diFiltered = power(bi, 4);
            return;
        case 14:
            biFiltered = bi;
            diFiltered = LUT(bi);
            return;
        case 15:
            biFiltered = bi;
            diFiltered = bitPlaneSlice(bi);
            return;
        case 16:
            biFiltered = bi;
            diFiltered = histogram(bi);
            return;
        }
    }

    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        if (cb.getActionCommand().equals("SetFilter")) {
            setOpIndex(cb.getSelectedIndex());
            repaint();
        } else if (cb.getActionCommand().equals("Formats")) {
            String format = (String) cb.getSelectedItem();
            File saveFile = new File("savedimage." + format);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(cb);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {
                    ImageIO.write(diFiltered, format, saveFile);
                } catch (IOException ex) {
                }
            }
        }
    };

    public static void main(String s[]) {
        JFrame f = new JFrame("Image Processing Demo (Lab 2)");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Lab5 de = new Lab5();
        f.add("Center", de);

        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);
        JPanel panel = new JPanel();
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }
}