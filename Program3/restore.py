import sys 
import os  
import boto3  
import botocore  

# Checks if the specified directory exists
def check_directory_existence(directory):
    if not os.path.exists(directory):
        print("Error: The specified directory does not exist:", directory)
        return False
    return True

# Establishes connection with AWS S3
def access_aws_s3():
    s3 = boto3.resource("s3")  # Creating an S3 resource object
    session = boto3.session.Session()  # Creating a session object
    region = session.region_name  # Retrieving the current region
    client = boto3.client('s3', region_name=region)  # Creating an S3 client object
    return s3, client

#Checks if the specified bucket exists in AWS S3
def check_bucket_existence(client, bucket_name, bucket_directory):
    try:
        client.head_bucket(Bucket=bucket_name)  # Checking if the bucket exists
        print(f"Restoring from Bucket [{bucket_name}], Directory in the Bucket:", bucket_directory)
        return True
    except botocore.exceptions.ClientError as e:
        print(f"Cannot Find Bucket [{bucket_name}] to Restore")
        print("Error: Cannot find Bucket [{bucket_name}] to Restore")
        return False

# Restores files from the specified bucket directory to the local directory.
def restore(bucket, bucket_directory, my_directory):    
    for obj in bucket.objects.filter(Prefix=bucket_directory):  # Iterating through objects in the bucket
        current_path = obj.key.replace(bucket_directory, my_directory)  # Calculating the current path
        if not os.path.exists(os.path.dirname(current_path)):  # Creating directories if they don't exist
            os.makedirs(os.path.dirname(current_path))
        if current_path[-1] != '/':  # Downloading files if the path does not end with '/'
            bucket.download_file(obj.key, current_path)
            
    print("-------------------------")
    print("RESTORE COMPLETED")


def main():
    if len(sys.argv) != 4:
        print("Invalid syntax. Usage: python3 restore.py restore directory-name bucket-name::directory-name")
        return
    
    if sys.argv[1] != "restore":
        print("Invalid command. Use: python3 restore.py restore directory-name bucket-name::directory-name")
        return

    restore_directory = sys.argv[3]  # Getting the restore directory from command-line arguments

    s3, client = access_aws_s3()  # Establishing connection with AWS S3

    s3_directory = sys.argv[2].split("::")  # Splitting bucket name and directory
    bucket_name = s3_directory[0]  # Extracting the bucket name
    bucket_directory = s3_directory[1]  # Extracting the bucket directory

    if not check_bucket_existence(client, bucket_name, bucket_directory):
        return

    bucket = s3.Bucket(bucket_name)  # Getting the S3 bucket object
    restore(bucket, bucket_directory, restore_directory)  # Restoring files

if __name__ == "__main__":
    main()
