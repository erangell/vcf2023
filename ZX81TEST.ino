/*
 * ZX81 keyboard control
*/
int pause= 240;
//int pause=720;  // when 16k card is plugged in





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


  letter(16); //PRINT
  func();
  letter(13);  //PI
  comma();
  func();
  letter(24); //EXP
  lparen();
  number(1);
  rparen();
  enter();

  memtest();

  
// helloworld()
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

 delay(pause*20);
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
  spacebar();
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

void spacebar()
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
