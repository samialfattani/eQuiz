package images;

import java.text.SimpleDateFormat 
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Desktop;

version = '2.12.0.1'
myDate = new SimpleDateFormat("d MMMM, yyyy").format(new Date()) 

file = new File('splash-blank-server.bmp')
bufferedImage = ImageIO.read(file)
x = 365
y = 45
g = bufferedImage.getGraphics();

g.setColor(Color.BLUE);
g.setFont(new Font("Courier New", Font.BOLD, 19));
g.drawString(version, x+10, y+10);

g.drawString(mydate, x+10, y+35);

f = new File ("splash-blank-server.bmp")
ImageIO.write(bufferedImage, "bmp", f)

println "Image Created"
Desktop.getDesktop().open(f)

