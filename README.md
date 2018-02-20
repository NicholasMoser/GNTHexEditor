GNT Hex Editor
=========

This project builds upon [Valentyn Kolesnikov's Hex Editor](https://github.com/javadev/hexeditor).
It is designed to be used as a modding tool for Naruto GNT files, particularly GNT4.
It is modular enough to be modified to add new tools as they are needed.
To run the Hex Editor you will need to first [download Java](https://java.com/en/download/). Then download the [hexeditor.jar](https://github.com/NicholasMoser/hexeditor/raw/master/hexeditor.jar) file and run it.

[![Screen short](https://raw.github.com/NicholasMoser/hexeditor/master/hexeditor.png)](https://github.com/NicholasMoser/hexeditor/)

Open the file you wish to modify. For this example I will use the GNT4 Translator tool.

[![Screen short](https://raw.github.com/NicholasMoser/hexeditor/master/hexeditor2.png)](https://github.com/NicholasMoser/hexeditor/)

In the above file, I have highlighted 16 bytes. Among these 16 bytes are four 4 byte pointers. Each pointer pointers to a location with shift-jis Japanese text. The interval for these values are the hex values 0x32C60 to 0x32C6F inclusive. Make sure your interval perfectly fits 4 byte pointers or else you'll get weird side effects.
This corresponds to the decimal values 207968 to 207983 (also inclusive). To translate the text at these pointers go to GNT4 Translation in the GNT menu option.

[![Screen short](https://raw.github.com/NicholasMoser/hexeditor/master/hexeditor3.png)](https://github.com/NicholasMoser/hexeditor/)

First it will ask for the starting point. Enter the starting decimal value, which in this example case is 207968. 

[![Screen short](https://raw.github.com/NicholasMoser/hexeditor/master/hexeditor4.png)](https://github.com/NicholasMoser/hexeditor/)

Now enter the ending pointer, which in this example case is 207983.

[![Screen short](https://raw.github.com/NicholasMoser/hexeditor/master/hexeditor5.png)](https://github.com/NicholasMoser/hexeditor/)

Now you can start translating the text! Enter your translation into the same text field as the original Japanese text. The current and total number of pointers to iterate through as displayed on each translation text box. You must match the same number of characters as the original Japanese text. If you fall under, you can fill with whitespace.

Newlines in the Japanese text are denoted by the ↓ character. If you wish to enter your own newline, you can use the `~` character or add `[LINE]` to your translation. You may add `[END]` to the end of your translated text, but it is not required.
