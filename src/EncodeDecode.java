// CS 576 - ASG 2
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class EncodeDecode implements MouseListener, MouseMotionListener 
{  
   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		int quantLevel = Integer.parseInt(args[1]);
   		if (quantLevel < 0 || quantLevel > 7) {
   			System.out.println("Quantization level should be b/w 0 & 7");
   		}
   		int deliveryMode = Integer.parseInt(args[2]);
   		int latency = Integer.parseInt(args[3]);

   		//String fileName = "../image1.rgb";
   		
   		EncodeDecode ir = new EncodeDecode(quantLevel, deliveryMode, latency, fileName);
   }
   
   
   // 8x8 block 
   class Block8x8 {
	   byte[] bytes = new byte[64];
   }
   
   
   // fields
   public static int width = 352; // width of image
   public static int height = 288; // height of image
   public static int quantizationLevel; // 0 - 7
   public static int  deliveryMode; // 1 || 2 || 3
   public static int latency; // in milliseconds
   public static Block8x8[] RBlocks; // blocks for R component
   public static Block8x8[] GBlocks; // blocks for G component
   public static Block8x8[] BBlocks; // blocks for B component
   
   public EncodeDecode(int quant, int mode, int lat, String fileName)
   {
	
	    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	    //Reading File
	    try {
		    File file = new File(fileName);
		    InputStream is = new FileInputStream(file);
	
		    long len = file.length();
		    byte[] bytes = new byte[(int)len];
		    
		    
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	    
	    	// read all the bytes
	    	int ind = 0;
			for(int y = 0; y < height; y++){
		
				for(int x = 0; x < width; x++){
			 
					//byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
			
			divideIntoBlocks(bytes);
			
			System.out.println("image divided");
			
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    // Use a label to display the image
	    JFrame frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    // Show original image
	    JLabel label = new JLabel(new ImageIcon(img));
	    label.setPreferredSize(new Dimension(width,height));
	    frame.getContentPane().add(label, BorderLayout.WEST);
	    
	    
	    // place holder for 2nd image
	    
	    // test image
	    BufferedImage img2 = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
	    int i=0;
	    for (int y = 0; y < 8; ++y) {
	    	for (int x = 0; x < 8; ++ x) {
	    		int pix = 0xff000000 | ((RBlocks[0].bytes[i] & 0xff) << 16) | ((GBlocks[0].bytes[i] & 0xff) << 8) | (BBlocks[0].bytes[i] & 0xff);
	    		img2.setRGB(x, y, pix);
	    		++i;
	    	}
	    }
	    
	    JLabel label2 = new JLabel(new ImageIcon(img2));
	    label2.setPreferredSize(new Dimension(width, height));
	    frame.getContentPane().add(label2, BorderLayout.EAST);

	    // Bottons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(width, 50));
	    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		
		MyButton closeButton = new MyButton("Close");
		buttonPanel.add(closeButton, BorderLayout.WEST);	
		
	    frame.pack();
	    frame.setVisible(true); 	
   }
   
   // divide each component (RGB) into 8x8 blocks
   public void divideIntoBlocks(byte bytes[]) {  
	   int ind; // track of the index in the block arrays
	   int Ri, Gi, Bi; // keep track of compoent in byte array
	   
	   // make blocks
	   RBlocks = new Block8x8[height*width];
	   GBlocks = new Block8x8[height*width];
	   BBlocks = new Block8x8[height*width];
	   for (int i = 0; i < height*width; ++i) {
		   RBlocks[i] = new Block8x8();
		   GBlocks[i] = new Block8x8();
		   BBlocks[i] = new Block8x8();
	   }
	   
	   // starting locations in byte array
	   Ri = 0; 
	   Gi = 0 + width * height;
	   Bi = 0 + width * height * 2;
	   ind = 0;
	   
	   int cornerR = 0; // saves upper left corner of a block
	   int cornerG = 0;
	   int cornerB = 0;
	   
	   // for each row of 8x8 blocks
	   for (int i = 0; i < height; i = i + 8) {
		   
		   // for each block in a row
		   for (int j = 0; j < width; j = j + 8) {
			   // save block's upper left corner
			   cornerR = Ri;
			   cornerG = Gi;
			   cornerB = Bi;
			   
			   int b = 0;
			   // for each row of bytes in the block
			   for (int k = 0; k < 8; ++k) {
				   
				   // for each byte in row in block
				   for (int l = 0; l < 8; ++l) {
					   System.out.println(Ri + " " + Gi + " " + Bi);
					   RBlocks[ind].bytes[b] = bytes[Ri];
					   GBlocks[ind].bytes[b] = bytes[Gi];
					   BBlocks[ind].bytes[b] = bytes[Bi];
					   ++b; ++Ri; ++Gi; ++Bi;
				   }
				   
				   //set Ri, Gi, Bi to first byte in next row in block
				   Ri = Ri - 8 + width;
				   Gi = Gi - 8 + width;
				   Bi = Bi - 8 + width;
			   }
			   ++ind; // new block!
			   // set Ri, Gi, Bi to upper left of next block in the row
			   Ri = cornerR + 8;
			   Gi = cornerG + 8;
			   Bi = cornerB + 8;
		   }
		   // Set Ri, Gi, Bi to next row's first block's upper left
		   Ri = cornerR - (width - 8) + (8 * width);
		   Gi = cornerG - (width - 8) + (8 * width); 
		   Bi = cornerB - (width - 8) + (8 * width);
	   }
   }
   
   
   // Function calls
	public void buttonPressed(String name)
	{
		if (name.equals("Split"))
		{
			//System.out.println("Split");
		} else if (name.equals("Initialize"))
		{
			//System.out.println("Initialize");
		} else if (name.equals("Reset"))
		{
			//System.out.println("Reset");
		} else if (name.equals("Close"))
		{
			//System.out.println("Close");
			System.exit(0);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println(arg0.getX() + " and " + arg0.getY());
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println("Moving");
	} 
	
	class MyButton extends JButton {
		MyButton(String label){
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) 
	  				{
						buttonPressed(getText());
					}
				}
			);
		}
		
		MyButton(String label, ImageIcon icon){
			Image img = icon.getImage();
			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaleimg));
			setText(label);
			setFont(new Font("Helvetica", Font.PLAIN, 0));
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) {
						buttonPressed(getText());
					}
				}
			);
		}
	}
}