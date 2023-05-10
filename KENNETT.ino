/*
 * ZX81 keyboard control
*/
int pause= 240;
//int pause=720;  // when 16k card is plugged in

int refresh = 8; //pause multiplier for screen refreshes


int printer = 1; //set to 1 to generate LPRINT statements



// the setup function runs once when you press reset or power the board
void setup() {
  // initialize digital pin LED_BUILTIN as an output.
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(2,OUTPUT);
  pinMode(3,OUTPUT);
  pinMode(4,OUTPUT);
  pinMode(5,OUTPUT);
  pinMode(6,OUTPUT);
  pinMode(7,OUTPUT);
  pinMode(8,OUTPUT);
  pinMode(9,OUTPUT);
  pinMode(A1,OUTPUT);
  pinMode(A2,OUTPUT);
  pinMode(A3,OUTPUT);
  pinMode(A4,OUTPUT);     
  pinMode(A5,OUTPUT);
  
  digitalWrite(A1,HIGH);
  digitalWrite(A2,HIGH);
  digitalWrite(A3,HIGH);
  digitalWrite(A4,HIGH);  
  digitalWrite(A5,LOW);

  //5 second countdown pattern
  for (int i=1; i<=5; i++)
  {
    digitalWrite(13,HIGH);
    delay(pause/2);
    digitalWrite(13,LOW);
    delay(pause/2);
  }

  enter();
  delay(pause*(refresh*3));
  letter(1); //NEW
  enter();
  delay(pause*(refresh*3));
  kennett();

//  memtest();
// helloworld()

}

void kennett()
{
  number(1);
  letter(5); //REM XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
  for (int i=0; i<16; i++) {
    letter(24) ; // X
  }
  enter();

  number(2);
  letter(12); //LET
  letter(1); //A
  dollar();
  shift();
  letter(12); //=
  letter(16); //"
  unshift();
  letter(11); //K
  letter(3); //C
  quote();
  enter();

  number(3);
  letter(16); //PRINT
  quote();
  letter(16); letter(18); letter(9); letter(14);
  letter(20); letter(9); letter(14); letter(7);
  period(); period(); period();
  quote();
  enter();
  
  number(4);
  letter(12); //LET
  letter(2); //B
  equal();
  number(1);
  number(6);
  number(5);
  number(1);
  number(4);
  enter();

  number(5);
  lprint(); //PRINT OR LPRINT
  quote();
  letter(9); space(); letter(8); letter(1); letter(4); space(); letter(6); letter(21); letter(14); space(); letter(1); letter(20); space();
  quote();
  semi();
  enter();

  number(6);
  lprint();
  quote();
  letter(11); letter(5); letter(14); letter(14); letter(5); letter(20); letter(20); space();
  letter(3); letter(12); letter(1); letter(19); letter(19); letter(9); letter(3);
  quote();
  enter();

  number(7);
  lprint();
  enter();

  number(9);
  letter(6); //for
  letter(12); //L
  equal();
  number(1);
  shift();
  number(4); //TO
  unshift();
  func();
  letter(11); //LEN
  letter(1);  //A
  dollar();
  enter();
  
  number(1); number(0);
  letter(12); //LET
  letter(16); //P
  equal();
  number(7); number(6); number(8); number(8);
  plus();
  lparen();
  func(); letter(9); //CODE
  letter(1); dollar();
  lparen();
  letter(12);
  rparen();
  minus();
  number(1);
  rparen();
  star();
  number(8);
  enter();

  //DEBUG
  /*
  number(1); number(1);
  lprint();
  letter(16);
  enter();
  */

  delay(pause);

  number(2); number(0);
  letter(6); //for
  letter(10); //J
  equal();
  number(0);
  shift();
  number(4); //TO
  unshift();
  number(7);
  enter();

  number(3); number(0);
  letter(12); //LET
  letter(1);  //A
  equal();
  func();
  letter(15); //PEEK
  lparen();
  letter(16); //P
  plus();
  letter(10); //J
  rparen();
  enter();

  number(4); number(0);
  letter(15); //POKE
  letter(2);  //B
  plus();
  letter(10); //J
  comma();
  letter(1);  //A
  enter();

  number(5); number(0);
  letter(14); //NEXT
  letter(10); //J
  enter();

  number(5); number(5);
  letter(6); //for
  letter(5); //E
  equal();
  number(7);
  shift();
  number(4); //TO
  unshift();
  number(0);
  shift() ;
  letter(5);  //STEP
  unshift();
  minus();
  number(1);
  enter();
  delay(pause);

  number(6); number(0);
  letter(6); //for
  letter(9); //i
  equal();
  letter(2);  //B
  shift();
  number(4); //TO
  unshift();
  letter(2);  //B+7
  plus();
  number(7);
  enter();
  delay(pause);

  
  number(6); number(1);
  letter(12) ; //LET
  letter(20); //T
  equal();
  func();
  letter(15); //PEEK
  letter(9);  //I
  enter();
  delay(pause*refresh);

  number(6); number(2);
  letter(21); //IF
  letter(20); //T
  shift();
  letter(14); // <
  unshift();
  number(2);
  shift();
  letter(8);  // **
  unshift();  
  letter(5);
  shift();
  number(3);  //THEN
  unshift();
  letter(7);  //GOTO 70
  number(7);
  number(0);
  enter();
  delay(pause*refresh);

  number(6); number(4);
  letter(12); //LET
  letter(20); //T
  equal();
  letter(20); //T
  minus();
  number(2);
  shift();
  letter(8);  // **
  unshift();  
  letter(5);
  enter();
  delay(pause*refresh);

  number(6); number(5);
  letter(15); //POKE
  letter(9);  //I
  comma();
  letter(20); //T
  enter();
  delay(pause*refresh);

  number(6); number(6);
  letter(15); //POKE
  number(1); number(6); number(5); number(2); number(2);
  plus();
  number(7);
  minus();
  letter(9); //I
  plus();
  letter(2); //B
  comma();
  number(1); number(3); number(6);
  enter();
  delay(pause*refresh);

  number(6); number(7);
  letter(7); //GOTO
  number(8); number(0);
  enter();
  delay(pause*refresh);

  number(7); number(0);
  letter(15); //POKE
  number(1); number(6); number(5); number(2); number(2);
  plus();
  number(7);
  minus();
  letter(9); //I
  plus();
  letter(2); //B
  comma();
  number(0);
  enter();
  delay(pause*refresh);
  
  number(8); number(0);
  letter(14); //NEXT
  letter(9); //I
  enter();
  delay(pause*(refresh*5));
  
  number(9); number(0);
  letter(6); //FOR
  letter(17); //Q
  equal();
  number(1); number(6); number(5); number(2); number(2);
  shift();
  number(4); //TO
  unshift();
  number(1); number(6); number(5); number(2); number(9);
  enter();
  delay(pause*(refresh*3));
  
  number(9); number(4);
  lprint();
  func();
  letter(21);
  lparen();
  func();
  letter(15); //PEEK
  letter(17); //Q
  rparen();
  semi();
  enter();
  delay(pause*(refresh*3));
  
  number(9); number(6);
  letter(14); //NEXT
  letter(17); //Q
  enter();
  delay(pause*(refresh*3));

  number(9); number(8);
  lprint();
  enter();
  delay(pause*(refresh*3));

  number(1); number(0); number(0);
  letter(14); //NEXT
  letter(5); //E
  enter();
  delay(pause*(refresh*3));

  number(1); number(1); number(0);
  letter(14); //NEXT
  letter(12); //L
  enter();
  delay(pause*(refresh*3));

  number(1); number(1); number(5);
  lprint();
  quote();
  letter(16); letter(18); letter(9); letter(14); letter(20); letter(5); letter(4); space();
  letter(15); letter(14); space(); letter(1); space();
  letter(20); letter(9); letter(13); letter(5);
  quote();
  semi();
  enter();
  delay(pause*(refresh*3));

  number(1); number(1); number(6);
  lprint();
  quote();
  letter(24);  space();
  letter(19); letter(9); letter(14); letter(3); letter(12); letter(1); letter(9); letter(18); space();
  number(1); number(0); number(0); number(0); 
  quote();
  enter();
  delay(pause*(refresh*3));

  number(1); number(2); number(0);
  lprint();
  quote();
  letter(12); letter(5); letter(1); letter(18); letter(14); space();
  letter(13); letter(15); letter(18); letter(5); space();
  letter(1); letter(20); space();
  quote();
  semi();
  enter();
  delay(pause*(refresh*3));

  number(1); number(2); number(1);
  lprint();
  quote();
  letter(11); letter(5); letter(14); letter(14); letter(5); letter(20); letter(20); 
  letter(3); letter(12); letter(1); letter(19); letter(19); letter(9); letter(3);
  quote();
  semi();
  enter();
  delay(pause*(refresh*3));

  
  number(1); number(2); number(2);
  lprint();
  quote();
  period();
  letter(3); letter(15); letter(13);
  quote();
  enter();
  delay(pause*(refresh*3));

  number(9); number(9); number(9);
  letter(16); //PRINT
  quote();
  letter(4); letter(15); letter(14); letter(5);
  quote();
  enter();
  delay(pause*(refresh*3));

  //llist();
  //enter();
  //delay(pause*100);
  
  letter(18); //RUN
  enter();
 
}

int lprint()
{
  if (printer == 1)
  {
    shift();
    letter(19);
    unshift();
  }
  else
  {
    letter(16);
  }
}

int llist()
{
  if (printer == 1)
  {
    shift();
    letter(7);
    unshift();
  }
  else
  {
    letter(11);
  }
  

}

void dollar()
{
  shift();
  letter(21);
  unshift();
}


void memtest()
{
  number(1);
  letter(12);  //1 LET
  letter(1);  //A
  shift();
  letter(12); // = 18000
  unshift();
  number(1);
  number(8);
  number(0);
  number(0);
  number(0);
  enter();
  
  pokeline(2,0,0,3,3);
  pokeline(3,1,0,1,1);
  pokeline(4,2,0,0,0);
  pokeline(5,3,0,5,7);
  pokeline(6,4,0,6,8);
  pokeline(7,5,0,7,7);
  pokeline(8,6,2,0,1);

  number(1); //10 LET U=USR(18000)
  number(0);
  letter(12); //LET
  letter(21);
  equal();
  func();     //USR
  letter(12);
  lparen();
  number(1);
  number(8);
  number(0);
  number(0);
  number(0); 
  rparen();
  enter();

  number(2);
  number(0);
  letter(16); //PRINT "u=";U
  quote();
  letter(21);
  equal();
  quote();
  semi();
  letter(21);
  enter();

  number(3);
  number(0);
  letter(12); //LET
  letter(14); //N=N-16373
  equal();
  letter(21);
  minus();
  number(1);
  number(6);
  number(3);
  number(7);
  number(3);
  enter();

  number(4);
  number(0);
  letter(16); //PRINT
  quote();
  letter(14); //N
  equal();
  quote();
  semi();
  letter(14);
  enter();

  number(5);
  number(0);
  letter(12); //LET
  letter(13); //M=N/1024
  equal();
  letter(14);
  slash();
  number(1);
  number(0);
  number(2);
  number(4);
  enter();

  number(6);
  number(0);
  letter(16); //PRINT
  quote();
  letter(13); //"MEM=";M
  letter(5);
  letter(13);
  equal();
  quote();
  semi();
  letter(13);
  enter();

 letter(18);  //run
 enter();

 delay(pause*30);
 shift();
 letter(7);  //Llist
 unshift();
 enter();
}

void pokeline (int line, int add, int num1, int num2, int num3)
{
  number(line);
  letter(15); //3 POKE A+1,11
  letter(1);
  plus();
  number(add);
  comma();
  number(num1);
  number(num2);
  number(num3);
  enter();
}

void helloworld()
{
  number(1);
  letter(16); //PRINT
  quote();  
  //HELLO WORLD
  letter(8);
  letter(5);
  letter(12);
  letter(12);
  letter(15);
  comma();
  space();
  quote();
  semi();
  enter();

  number(2);
  letter(16); //PRINT
  quote();
  letter(23);
  letter(15);
  letter(18);
  letter(12);
  letter(4); 
  quote();
  enter();
  
  number(3);

  letter(7);  //GOTO
  number(1);
  enter();
  
  //RUN
  letter(18); //RUN 
  enter();
}

void enter()
{
      digitalWrite(8,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(8,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);
      delay(1500);
}

void space()
{
      digitalWrite(9,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(9,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);
}
void period()
{
      digitalWrite(9,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(9,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);    
}
void comma()
{
  shift();
  period();
  unshift();
}

void lparen()
{
  shift();
  letter(9);
  unshift();
}
void rparen()
{
  shift();
  letter(15);
  unshift();
}
void semi() // SHIFT X
{
  shift();
  letter(24);
  unshift();
}

void plus() // SHIFT K
{
  shift();
  letter(11);
  unshift();
}

void minus() // SHIFT J
{
  shift();
  letter(10);
  unshift();
}
void star() // SHIFT B
{
  shift();
  letter(2);
  unshift();
}
void slash() // SHIFT V
{
  shift();
  letter(22);
  unshift();
}
void equal() // SHIFT L
{
  shift();
  letter(12);
  unshift();
}
void shift()
{
  digitalWrite(13,HIGH);
  delay(pause/2);
}
void unshift()
{
  digitalWrite(13,LOW);
  delay(pause/2);  
}

void func()
{
  shift();
  digitalWrite(8,HIGH);
  digitalWrite(A5,HIGH);
  delay(pause/2);
  digitalWrite(8,LOW);
  digitalWrite(A5,LOW);
  delay(pause/2);
  unshift();
}

void quote()  // SHIFT P
{
  shift();
  letter(16);
  unshift();
}

void number (int i)
{
  switch(i)
  {
    case 1:
    {
      digitalWrite(2,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(2,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);
      break;
    }
    case 2:
    {
      digitalWrite(2,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(2,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;      
    }
    case 3:
    {
      digitalWrite(2,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(2,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;      
    }
    case 4:
    {
      digitalWrite(2,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(2,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;      
    }
    case 5:
    {
      digitalWrite(2,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(2,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;      
    }
    case 0:
    {
      digitalWrite(4,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(4,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);
      break;
    }
    case 9:
    {
      digitalWrite(4,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(4,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;      
    }
    case 8:
    {
      digitalWrite(4,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(4,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;      
    }
    case 7:
    {
      digitalWrite(4,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(4,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;      
    }
    case 6:
    {
      digitalWrite(4,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(4,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;      
    }
  }
}
void letter(int i)
{
  switch (i)
  {
    case 1: {
      digitalWrite(5,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(5,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2); 
      break;
    }
    case 2: {
      digitalWrite(9,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(9,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2); 
      break;
    }
    case 3: {
      digitalWrite(7,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(7,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2); 
      break;
    }
    case 4: {
      digitalWrite(5,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(5,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2); 
      break;
    }
    case 5: {
      digitalWrite(3,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(3,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2); 
      break;
    }
    case 6: {
      digitalWrite(5,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(5,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2); 
      break;
    }
    case 7: {
      digitalWrite(5,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(5,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;
    }
    case 8: {
      digitalWrite(8,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(8,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;
    }
    case 9: {
      digitalWrite(6,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(6,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;
    }
    case 10: {
      digitalWrite(8,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(8,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;
    }
    case 11: {
      digitalWrite(8,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(8,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;
    }
    case 12: {
      digitalWrite(8,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(8,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;
    }
    case 13: {
      digitalWrite(9,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(9,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;
    }
    case 14: {
      digitalWrite(9,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(9,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;
    }
    case 15: {
      digitalWrite(6,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(6,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;
   }
   case 16: {
      digitalWrite(6,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(6,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);  
      break;
   }
   case 17: {
      digitalWrite(3,HIGH);
      digitalWrite(A5,HIGH);
      delay(pause/2);
      digitalWrite(3,LOW);
      digitalWrite(A5,LOW);
      delay(pause/2);
      break;
   }
   case 18: {
      digitalWrite(3,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(3,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;
    }
   case 19: {
      digitalWrite(5,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(5,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;
    }
   case 20: {
      digitalWrite(3,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(3,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;
    }
    case 21: {
      digitalWrite(6,HIGH);
      digitalWrite(A2,LOW);
      delay(pause/2);
      digitalWrite(6,LOW);
      digitalWrite(A2,HIGH);
      delay(pause/2);
      break;
    }   
    case 22: {
      digitalWrite(7,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(7,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;
    }
    case 23: {
      digitalWrite(3,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(3,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;
    }        
    case 24: {
      digitalWrite(7,HIGH);
      digitalWrite(A3,LOW);
      delay(pause/2);
      digitalWrite(7,LOW);
      digitalWrite(A3,HIGH);
      delay(pause/2);
      break;
    }    
    case 25: {
      digitalWrite(6,HIGH);
      digitalWrite(A1,LOW);
      delay(pause/2);
      digitalWrite(6,LOW);
      digitalWrite(A1,HIGH);
      delay(pause/2);
      break;
    }
    case 26: {
      digitalWrite(7,HIGH);
      digitalWrite(A4,LOW);
      delay(pause/2);
      digitalWrite(7,LOW);
      digitalWrite(A4,HIGH);
      delay(pause/2);
      break;     
    }
  }
}


void loop(){
  //test1()
}

// the loop function runs over and over again forever
void test1() {
  digitalWrite(LED_BUILTIN, LOW);    // turn the LED off by making the voltage LOW
  delay(pause);                       // wait for a second

  test();
  
  digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(pause);                       // wait for a second

  test();
}

void test()
{
 
  digitalWrite(A1, LOW);  //turn on A1 thru A4 using LOW
  delay(pause);        
  
  for (int p=2; p<=9; p++)
  {
    digitalWrite(p, HIGH);
    delay(pause/2);        
    digitalWrite(p, LOW);
    delay(pause/2);        
  }
  
  
  digitalWrite(A1,HIGH);
  delay(pause);    

  digitalWrite(A2, LOW);  
  delay(pause);        

  for (int p=2; p<=9; p++)
  {
    digitalWrite(p, HIGH);
    delay(pause/2);        
    digitalWrite(p, LOW);
    delay(pause/2);        
  }
  
  digitalWrite(A2,HIGH);
  delay(pause);    
  
  digitalWrite(A3, LOW);
  delay(pause);        
  
  for (int p=2; p<=9; p++)
  {
    digitalWrite(p, HIGH);
    delay(pause/2);        
    digitalWrite(p, LOW);
    delay(pause/2);        
  }
  
  digitalWrite(A3,HIGH);
  delay(pause);    
  
  digitalWrite(A4, LOW);
  delay(pause);        
  
  for (int p=2; p<=9; p++)
  {
    digitalWrite(p, HIGH);
    delay(pause/2);        
    digitalWrite(p, LOW);
    delay(pause/2);        
  }
  
  digitalWrite(A4,HIGH);
  delay(pause);    
  
  digitalWrite(A5, HIGH);
  delay(pause);        
  
  for (int p=2; p<=9; p++)
  {
    digitalWrite(p, HIGH);
    delay(pause/2);        
    digitalWrite(p, LOW);
    delay(pause/2);        
  }
  
  digitalWrite(A5,LOW);
  delay(pause);        

  // " = SHIFT P
  digitalWrite(13,HIGH);
  delay(pause/2);
  digitalWrite(6,HIGH);
  digitalWrite(A5,HIGH);
  delay(pause/2);
  digitalWrite(6,LOW);
  digitalWrite(A5,LOW);
  delay(pause/2);
  digitalWrite(13,LOW);
  delay(pause/2);

  //ENTER
  digitalWrite(7,HIGH);
  digitalWrite(A5,HIGH);
  delay(pause/2);
  digitalWrite(7,LOW);
  digitalWrite(A5,LOW);
  delay(pause/2);

}
