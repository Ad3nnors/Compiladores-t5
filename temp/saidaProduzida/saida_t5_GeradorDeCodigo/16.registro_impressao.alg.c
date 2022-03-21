#include <stdio.h>
#include <stdlib.h>


int main() {
struct {
char* nome;
int idade;
} reg;
reg.nome = "Maria";
reg.idade = 24;
printf("eg.nom");
printf(" tem ");
printf("%d", reg.idade);
printf(" anos");
return 0;
}
