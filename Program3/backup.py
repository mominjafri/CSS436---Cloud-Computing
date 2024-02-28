import boto3 
import botocore 
import sys  
import os  
import datetime  

def backup(directory, bucket_path):
    """
    Backs up files from the specified directory to AWS S3.

    """
    # Checking if the directory exists
    if not os.path.exists(directory): 
        print(f"The directory '{directory}' does not exist")
        return

    # Getting access to AWS S3
    s3 = boto3.resource("s3")  # Creating an S3 resource object
    client = boto3.client('s3')  # Creating an S3 client object
    bucket_name, s3_directory = bucket_path.split("::")  # Extracting bucket name and directory
    session = boto3.session.Session()  # Creating a session object to get the region name
    region = session.region_name  # Getting the AWS region name

    # Checking if the bucket exists
    try:
        client.head_bucket(Bucket=bucket_name)  # Checking if the bucket exists
        print(f"Backing up to [{bucket_name}], Directory: {s3_directory}")
    except botocore.exceptions.ClientError:  # Handling client errors
        try: 
            # Creating bucket if it doesn't exist
            s3.create_bucket(Bucket=bucket_name, CreateBucketConfiguration={'LocationConstraint': region})
            print(f"Backing up to Bucket [{bucket_name}] just created.")
        except s3.meta.client.exceptions.BucketAlreadyExists:
            print(f"Cannot find Bucket [{bucket_name}] and fail to create.")
            return

    # Traversing through the directory and its subdirectories to backup files
    for root, dirs, files in os.walk(directory):
        print("-------------------------")
        print("Path:", root)
        print("Directory:", dirs)
        print("Files:", files)

        last_path = directory.split("/")  # Extracting the last part of the directory
        backup_path = s3_directory + "/" + last_path[-1]  # Creating the backup path in S3
        current_path = root.replace(directory, backup_path)  # Replacing local directory with S3 backup directory
        print("Current path: ", current_path)

        for file in files:
            file_path = os.path.join(root, file)  # Creating full file path
            bucket_file_path = current_path + "/" + file  # Creating full bucket file path

            try: 
                obj = s3.meta.client.head_object(Bucket=bucket_name, Key=bucket_file_path)  # Getting object metadata

                latest_version = datetime.datetime.fromtimestamp(os.path.getmtime(file_path), tz=datetime.timezone.utc)  # Getting latest file modification time
                if obj["LastModified"] < latest_version:  # Checking if file needs to be updated
                    try:
                        s3.Object(bucket_name, bucket_file_path).put(Body=open(file_path, "rb"))  # Uploading file to S3
                        print("Updated:", bucket_file_path)
                    except botocore.exceptions.ClientError:
                        print("Failed to Back Up - file:", file)

            except botocore.exceptions.ClientError:
                try:
                    s3.Object(bucket_name, bucket_file_path).put(Body=open(file_path, "rb"))  # Uploading file to S3
                    print("Uploaded:", bucket_file_path)
                except botocore.exceptions.ClientError:
                    print("Failed to Back Up - file:", file)

    print("-------------------------")
    print("BACK UP SUCCESSFUL")

def main():

    if len(sys.argv) != 4:
        print("Invalid syntax. Usage: python3 backup.py backup directory-name bucket-name::directory-name")
        return

    operation = sys.argv[1]  # Extracting operation from command-line arguments
    directory = sys.argv[2]  # Extracting directory from command-line arguments
    bucket_path = sys.argv[3]  # Extracting bucket path from command-line arguments

    if operation != "backup":
        print("Invalid syntax, use: python3 backup.py backup directory-name bucket-name::directory-name")
        return

    backup(directory, bucket_path)

if __name__ == "__main__":
    main()







