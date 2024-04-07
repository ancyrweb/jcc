int main() {
    int a = 100;
    int *b = &a;

    if (*b == 100) {
        return 0;
    }

    return *b;
}

int square(int a) {
    return 10;
}