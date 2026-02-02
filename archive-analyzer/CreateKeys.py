from cryptography.hazmat.primitives import serialization as crypto_serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.backends import default_backend as crypto_default_backend
from json import load

def createSSHKey():
    key = rsa.generate_private_key(
        backend=crypto_default_backend(),
        public_exponent=65537,
        key_size=2048
    )

    private_key = key.private_bytes(
        crypto_serialization.Encoding.PEM,
        crypto_serialization.PrivateFormat.PKCS8,
        crypto_serialization.NoEncryption()
    )

    public_key = key.public_key().public_bytes(
        crypto_serialization.Encoding.OpenSSH,
        crypto_serialization.PublicFormat.OpenSSH
    )

    return private_key, public_key

if __name__ == '__main__':
    with open("config.json") as config_file:
        config = load(config_file)

    for i in range(config["namenodes"]):
        private_key, public_key = createSSHKey()
        with open(f"keys/namenode_{i}_id_rsa", "wb") as private_key_file:
            private_key_file.write(private_key)

        with open(f"keys/namenode_{i}_id_rsa.pub", "wb") as public_key_file:
            public_key_file.write(public_key)

    for i in range(config["datanodes"]):
        private_key, public_key = createSSHKey()
        with open(f"keys/datanode_{i}_id_rsa", "wb") as private_key_file:
            private_key_file.write(private_key)

        with open(f"keys/datanode_{i}_id_rsa.pub", "wb") as public_key_file:
            public_key_file.write(public_key)
