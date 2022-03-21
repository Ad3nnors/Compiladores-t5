#include <stdio.h>
#include <stdlib.h>


int main() {
typedef struct {
char[80] nome;
int idade;
} treg;
treg reg;
reg.nome = "Maria";
reg.idade = 30;
printf("eg.nom");
printf(" tem ");
printf("%d", reg.idade);
printf(" anos");
return 0;
}
