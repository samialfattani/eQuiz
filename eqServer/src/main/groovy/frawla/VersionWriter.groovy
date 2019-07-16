
	import java.awt.Color;
	import java.awt.Font;
	import java.awt.image.BufferedImage;
	import javax.imageio.ImageIO;
	import java.awt.Desktop;
	
	file = new File('splash-server.bmp') //splash-blank.bmp
	bufferedImage = ImageIO.read(file)
	x = 380
	y = 45
	g = bufferedImage.getGraphics();
	g.setColor(new Color(240, 240,  240));
	g.fillRect(x, y, 220, 20);
	
	g.setColor(Color.BLACK);
	g.setFont(new Font("Courier New", Font.BOLD, 20));
	g.drawString("my string", x+10, y+10);
	
	f = new File ("imagex.jpg")
	ImageIO.write(bufferedImage, "jpg", f)
	
	println "Image Created"
	
	Desktop.getDesktop().open(f)
	
	
