/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserInterface;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import utilities.ImageProcessor;

/**
 *
 * @author Gaurav-Punjabi
 * This is the main class the captures the image from webcam,detects the laser pointer and processes the 
 * gesture.
 * The basic logic for processing the gesture is pretty simple.
 * We simply calculate the the displacement of the laser point of both X-Axis and Y-Axis.
 * Then we compare the which displacement is greater X or Y.
 * Then further we check whehter the displacement is greater than the minimum threshold value.
 * Then finally we check if the displacement if +ve or -ve to check which gesture is it.S
 * 
 * For understanding further details about this process refer the code below.
 */
public class LaserTest extends javax.swing.JFrame {

    /**
     * Creates new form LaserTest
     * Creates a new Thread that captures the images from webcam and processes them and then recognizes the 
     * gesture.
     */
    public LaserTest() {
        initComponents();
        customInit();   
    }

    /**
    * This method basically initializes the native OpenCV library.
    * Then it starts the thread for capturing images from webcam.
    * Also initializes the object of robot that would generate the events when gesture is recognized.
    **/
    private void customInit() {
    	//Loading the native library
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        //Starting the image capturing thread.
        new Thread() {
            @Override
            public void run() {
                video();
            }
        }.start();

        //Initializing the robot Object to generate events;
    	try {
            robot = new Robot();
        } catch(AWTException awte) {
            System.out.println("AWT Exception caught : " + awte.getMessage());
        } catch(Exception e) {
            System.out.println("Exception caught : " + e.getMessage());
        }
    }

    /**
    * This method is invoked by the thread created in the customInit Method.
    * It capture the image from the webcam using VideoCapture Class with a resolution of 1366,768.
    * Then detects the laser-pointer,calculates the co-ordinates,takes the displacement and recognizes
    * the gesture created by the pointer.
	* 
	******************************************************************************************************
	*										 LASER DETECTION LOGIC
	*	1) First we take the captured image and then apply a threshold value to it using threshold method of
	* 		Imgproc class of OpenCV.This threshold returns a image that has the objects with bright colors.
	* 		Then we apply the inRange method to take create a mask of the objects with red color.
	*******************************************************************************************************
	*										 GESTURE RECOGNITION LOGIC
	* 	We have used 2 flags in this process
	* 		-isAnchorPoint = used to indicate if there is an anchor point present from th previous images.
	* 						 Anchor point is nothing but a point from which we calculate the displacement
	* 						 of the laser pointer.
	* 		-gestureRecognize = used to indicate that a gesture Has been recognized and no further displacement 
	* 							should be calculated unless and until a new anchorPoint is found.
	*	1) First we try to get Location of the pointer on the current image.If the co-ordinates are found the we
	* 	   process further otherwise we set anchorPoint and gestureRecognized to false.
	*   2) If the co-ordinates are found then we check if there is a anchor point present or not if it is not 
	* 	   present then we set the current position as anchorPoint co-ordinates and set anchorPoint to true.
	*	   If the anchor point is found then we proceed to Step-3.
	*   3) We calculate the X, Y displacement by subtracting the current co-ordinates and anchorPoint co-ordinates
	* 	   Then we check which displacement is greater(X/Y);
	*	4) Then we check whether the displacement is +ve or -ve and perform the respective operations when gesture
	*      is recognized.
    **/
    private void video() { 
    	//Intitializing videoCapture object that would capture the images from default in-built webcam.   	
        VideoCapture capture = new VideoCapture(0);
        
        //Initliaizing the matrix in which the images captured from webcam would be stored.
        Mat webcamMatImage = new Mat();
        
        //Setting the resoultion of the webcam.
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,1366);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,768);

        Mat mat = new Mat(3,3,CvType.CV_8U,new Scalar(1,1,1));
        
		if( capture.isOpened()){  
			while (true){  
				capture.read(webcamMatImage);  
				if( !webcamMatImage.empty() ){  
                    Mat inRange = new Mat();
                    Mat temp = new Mat();
                    //Converting the image to the provided threshold value.
                    Imgproc.threshold(webcamMatImage,inRange, 150, 255, Imgproc.THRESH_BINARY);
                    //Extracting only red color objects from the image.
                    Core.inRange(inRange, new Scalar(0,0,250), new Scalar(0,0,255), temp);
                    //Checking if laser is present or not in the image.
                    if(getX(temp) != -1) {
                    	//Checking if anchorPoint is present or not.
                        if(!isAnchorPoint || gestureDetected) {
                        	//Setting the anchor points
                            this.anchorX = getX(temp);
                            this.anchorY = getY(temp);

                            this.isAnchorPoint = true;
                        }
                        else {
                            //Calculating the x and y displacement.
                            int x = getX(temp) - this.anchorX;
                            int y = getY(temp) - this.anchorY;
                            //Checking which if X displcement is greater or Y.
                            if((x > y && ( x >=0 || y >= 0)) || (x < y && (x<0 || y < 0))) {
                                //Validating if the displacement is +ve or -ve.
                                if(x >= 200) {
                                    robot.keyPress(KeyEvent.VK_RIGHT);
                                    gestureDetected = true;
                                }
                                else if( x <= -200) {
                                    robot.keyPress(KeyEvent.VK_LEFT);
                                    gestureDetected = true;
                                }
                            }
                        }
                    }
                    else {
                        this.isAnchorPoint = false;
                        this.gestureDetected = false;
                    }
                    this.mat = temp;
                    this.jlDisplay.setIcon(new ImageIcon(ImageProcessor.toBufferedImage(temp)));
				}  
				else{  
					System.out.println(" -- Frame not captured -- Break!"); 
					break;  
				}
			}  
		}
		else{
			System.out.println("Couldn't open capture.");
		}
    }
    /**
    * This function traverses the given matrix to check the white pixels and returns the X-co-ordinate of 
    * of those pixels.
    * @param : source : The matrix in which pointer needs to extracted.
    * @returns : X-Cordinate of the pixel if found otherwise it returns -1;
    **/
    private int getX(Mat source) {
        byte[] buffer =  new byte[(int)(source.total() * source.elemSize())];
        source.get(0, 0, buffer);
        for(int i=0;i<source.height();i++) {
            for(int j=0;j<source.width();j++) {
                if(buffer[(source.width()*i) + j] == -1) {
                    return j;
                }
            }
        }
      
        return -1;
    }
    /**
    * This function traverses the given matrix to check the white pixels and returns the Y-co-ordinate of 
    * of those pixels.
    * @param : source : The matrix in which pointer needs to extracted.
    * @returns : Y-Cordinate of the pixel if found otherwise it returns -1;
    **/
    private int getY(Mat source) {
        byte[] buffer =  new byte[(int)(source.total() * source.elemSize())];
        source.get(0, 0, buffer);
        for(int i=0;i<source.height();i++) {
            for(int j=0;j<source.width();j++) {
                if(buffer[(source.width()*i) + j] == -1) {
                    return i;
                }
            }
        }
        return -1;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsp = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jlDisplay = new javax.swing.JLabel();
        jtf2 = new javax.swing.JTextField();
        jtf1 = new javax.swing.JTextField();
        jtf3 = new javax.swing.JTextField();
        jtf4 = new javax.swing.JTextField();
        jtf5 = new javax.swing.JTextField();
        jtf6 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jlDisplay.setIcon(new javax.swing.ImageIcon("C:\\Users\\tamanna\\Desktop\\Laser Sample\\Sample2.JPG")); // NOI18N
        jPanel1.add(jlDisplay);

        jsp.setViewportView(jPanel1);

        jtf2.setText("100");

        jtf1.setText("255");

        jtf3.setText("90.2");

        jtf4.setText("255");

        jtf5.setText("100");

        jtf6.setText("100");

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jsp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(jtf1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jtf2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jtf3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jtf4, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jtf5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jtf6, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtf2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtf1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtf3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtf4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtf5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtf6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsp, javax.swing.GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        byte[] buffer = new byte[(int)(mat.total() * mat.elemSize())];
        this.mat.get(0, 0, buffer);
        for(byte b : buffer) {
            if(b != 0)
                System.out.println("b = " + b);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LaserTest().setVisible(true);
            }
        });
    }
    private Mat mat;
    private boolean isAnchorPoint = false;
    private boolean gestureDetected = false;
    private int anchorX,anchorY;
    private Robot robot;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jlDisplay;
    private javax.swing.JScrollPane jsp;
    private javax.swing.JTextField jtf1;
    private javax.swing.JTextField jtf2;
    private javax.swing.JTextField jtf3;
    private javax.swing.JTextField jtf4;
    private javax.swing.JTextField jtf5;
    private javax.swing.JTextField jtf6;
    // End of variables declaration//GEN-END:variables
}
